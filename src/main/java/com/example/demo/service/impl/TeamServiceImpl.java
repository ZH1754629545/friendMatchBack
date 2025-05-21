package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ErrorCode;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.domain.Team;
import com.example.demo.model.domain.User;
import com.example.demo.model.domain.UserTeam;
import com.example.demo.model.dto.TeamQuery;
import com.example.demo.model.enums.TeamStatusEnum;
import com.example.demo.model.request.TeamJoinRequest;
import com.example.demo.model.vo.UserTeamVO;
import com.example.demo.model.vo.UserVO;
import com.example.demo.service.TeamService;
import com.example.demo.mapper.TeamMapper;
import com.example.demo.service.UserService;
import com.example.demo.service.UserTeamService;
import com.example.demo.utils.commonUtils.Compare;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author Lenovo
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-12-06 16:41:07
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {

        //1.请求参数不能为空
        if(team==null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.必须要有登录
        if(loginUser==null)
        {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long loginUserId = loginUser.getId();
        //3.校验信息
        //1.队伍人数上限制
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum<1||maxNum>20)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不合规");
        }
        //2.队伍描述限制字数
        String description = team.getDescription();
        if(description==null||description.length()>512)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述不合规");
        }
        //3.队伍标题<=20
        String name = team.getName();
        if(name==null||name.length()>20)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不合规");
        }
        //4.status 是否为公开

        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(teamStatusEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不合规");
        }
        //5.如果status是加密状态一定要密码

        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            String password = team.getPassword();
            if(password==null|| StringUtils.isBlank(password)||password.length()>32)
            {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置不正确");
            }
        }
        //时间校验：有效时间一定要大于当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间大于当前时间");
        }
        // 7. 校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUserId);
        long teamNum = this.count(queryWrapper);
        if(teamNum>=5)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建 5 个队伍");
        }

        //8.插入user信息到team
        team.setUserId(loginUserId);
        team.setId(null);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if(!result||teamId==null)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        //9.插入用户-队伍关联信息表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(loginUserId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if(!result||userTeam.getId()==null)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍-用户关联失败");
        }

        return teamId;
    }


    @Override
    public List<UserTeamVO> listUserTeamVO(TeamQuery teamQuery, Boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long>idList = teamQuery.getIdList();
            if(!CollectionUtils.isEmpty(idList)) {
                queryWrapper.in("id",idList);
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }

            //联合名字和描述
            String searchText=teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw->qw.like("name",searchText).or().like("description",searchText));
            }
            Integer maxNum = teamQuery.getMaxNum();
            // 查询最大人数相等的
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            //根据status查询
            Integer status = teamQuery.getStatus();
            //如果为空则默认搜索公开的
            if(status==null) {
                status = TeamStatusEnum.PUBLIC.getValue();
            }
            queryWrapper.eq("status",status);


            //会阻止用户查看加密房间
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
 /*           if(isAdmin|| teamStatusEnum.equals(TeamStatusEnum.PUBLIC))
            {
                queryWrapper.eq("status",status);
            }*/
 /*           if(!isAdmin&&!teamStatusEnum.equals(TeamStatusEnum.PUBLIC)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
*/
        }
        //过滤超时的team
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        //所有查询符合的team列表
        List<Team>teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList))
        {
            return new ArrayList<>();
        }
        //查询队伍队长信息
        List<UserTeamVO> userTeamVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if(userId==null) continue;
            //获取User
            User user =userService.getById(userId);
            //设置TeamVO
            UserTeamVO userTeamVO = new UserTeamVO();
            BeanUtils.copyProperties(team, userTeamVO);
            if(user!=null)
            {
                //设置UserVo
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user,userVO);
                //设置队长属性到teamVO
                userTeamVO.setUserVO(userVO);
            }
            userTeamVOList.add(userTeamVO);
        }

        return userTeamVOList;
    }

    @Override
    public List<UserTeamVO> myJoinTeam(TeamQuery teamQuery) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if(teamQuery == null) throw new BusinessException(ErrorCode.PARAMS_ERROR) ;

        List<Long>idList = teamQuery.getIdList();

        if(CollectionUtils.isEmpty(idList)) throw new BusinessException(ErrorCode.PARAMS_ERROR,"没有加入的队伍");
        queryWrapper.in("id",idList);
        //所有查询符合的team列表
        List<Team>teamList = this.list(queryWrapper);
        //查询队伍队长信息
        List<UserTeamVO> userTeamVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if(userId==null) continue;
            //获取User
            User user =userService.getById(userId);
            //设置TeamVO
            UserTeamVO userTeamVO = new UserTeamVO();
            BeanUtils.copyProperties(team, userTeamVO);
            if(user!=null)
            {
                //设置UserVo
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user,userVO);
                //设置队长属性到teamVO
                userTeamVO.setUserVO(userVO);
            }
            userTeamVOList.add(userTeamVO);
        }

        return userTeamVOList;
    }

    @Override
    public boolean updateTeam(Team team,User loginUser) {
        if(team == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long id = team.getId();
        if(id==null||id<=0) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        //老队伍
        Team oldTeam = this.getById(id);
        if(oldTeam==null) throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");

        //权限判断
        if(loginUser.getId()!=oldTeam.getUserId()&&!userService.isAdmin(loginUser)) throw new BusinessException(ErrorCode.NO_AUTH);

        //判断新老是否一致
        //TODO
        if(Compare.Contain(oldTeam,team)) throw new BusinessException(ErrorCode.PARAMS_ERROR,"数据没有发生变化");

        Team updateTeam = new Team();
        BeanUtils.copyProperties(team,updateTeam);
        boolean result = this.updateById(updateTeam);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitByTeamId(Team team, User loginUser) {
        if (team == null || loginUser == null || team.getId() == null || team.getId() <= 0 || loginUser.getId() <= 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long teamId = team.getId();
        Long userId = loginUser.getId();
        //判断这个人是否在队伍里
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);
        long is_exist = userTeamService.count(queryWrapper);
        if (is_exist <= 0) throw new BusinessException(ErrorCode.NULL_ERROR, "用户不在队伍里");
        //获取队伍目前人数，如果是最后一人则需要解散队伍，如果是队长则需要转交队长权限给下一个人
        //TODO
        QueryWrapper<UserTeam> queryUTByTeamId = new QueryWrapper<>();
        queryUTByTeamId.eq("teamId", teamId);
        long count = userTeamService.count(queryUTByTeamId);
        //最后一个人解散队伍 删除队伍信息
        if (count == 1) {
            //删除team信息
            boolean deleteTeam = this.removeById(teamId);
            if (!deleteTeam) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        } else if (count > 1) {
            //判断是否为队长
            Team teamInfo = this.getById(teamId);
            if (teamInfo.getUserId().equals(userId)) {
                //转交队长权限
                QueryWrapper<UserTeam> findNextLeader = new QueryWrapper<>();
                findNextLeader.eq("teamId", teamId);
                findNextLeader.last("order by id asc limit 2");
                List<UserTeam> userTeams = userTeamService.list(findNextLeader);
                if (CollectionUtils.isEmpty(userTeams) || userTeams.size() < 2) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询下一个队长失败");
                }
                UserTeam nextLeader = userTeams.get(1);
                //更新Team表队长信息
                Team updateTeam = new Team();
                updateTeam.setUserId(nextLeader.getUserId());
                updateTeam.setId(teamId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队长失败");
                }
            }
        }
        boolean result = userTeamService.remove(queryWrapper);
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍成员失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Long id, User loginUser) {
        //判断当前id是否为队长或者管理员
        Long UserId = loginUser.getId();
        Team team = this.getById(id);
        if(team==null) throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        if(!team.getUserId().equals(UserId) && !userService.isAdmin(loginUser)) throw new BusinessException(ErrorCode.NO_AUTH);
        //删除team
        boolean result = this.removeById(id);
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        //删除对应的UserTeam
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", id);
        boolean deleteTeam = userTeamService.remove(queryWrapper);
        if(!deleteTeam) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍成员失败");

        return true;
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if(teamJoinRequest==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        Long teamId = teamJoinRequest.getTeamId();
        //获取队伍信息
        Team team = this.getById(teamId);
        if(team==null) throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        if(team.getExpireTime()!=null&&team.getExpireTime().before(new Date())) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍已过期");
        //队伍状态
        Integer status = team.getStatus();

        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        assert teamStatusEnum != null;
        if(teamStatusEnum.equals(TeamStatusEnum.PRIVATE)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "队伍是私密队伍，无法加入");
        }

        //判断队伍是否加密过
        if(teamStatusEnum.equals(TeamStatusEnum.SECRET)) {
            String passWord = teamJoinRequest.getPassword();
            if(StringUtils.isBlank(passWord)) throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍是加密队伍，需要密码");
            if(!passWord.equals(team.getPassword())) throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        //改用户加入的队伍
        Long userId = loginUser.getId();
        //只有一个线程能执行
        RLock lock=redissonClient.getLock("zh:join_team");
        while(true){
            try {
                if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                    System.out.println("获取到锁:"+Thread.currentThread().getId());
                    //判断用户加入过几个队伍不超过五个
                    QueryWrapper<UserTeam>userCountQuery = new QueryWrapper<>();
                    userCountQuery.eq("userId", userId);
                    long hasJoin = userTeamService.count(userCountQuery);
                    if(hasJoin>=5) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户加入的队伍超过了五个");
                    }
                    //判断用户是否加入过此队伍
                    QueryWrapper<UserTeam>isExist = new QueryWrapper<>();
                    isExist.eq("userId", userId).eq("teamId", teamId);
                    long hasUserJoinThisTeam = userTeamService.count(isExist);
                    if(hasUserJoinThisTeam>0) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户已经加入过此队伍");
                    }
                    //判断队伍是否满员
                    QueryWrapper<UserTeam>TeamCountQuery = new QueryWrapper<>();
                    TeamCountQuery.eq("teamId", teamId);
                    long hasTeam = userTeamService.count(TeamCountQuery);
                    if(hasTeam>=team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍人数已满");
                    }

                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }catch (InterruptedException e){
                log.error("joinTeam error",e);
                return false;
            }finally {
                //释放自己的锁
                if(lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        }
    }

}




