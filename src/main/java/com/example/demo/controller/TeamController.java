package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.BaseResponse;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.ResultUtils;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.domain.Team;
import com.example.demo.model.domain.User;
import com.example.demo.model.domain.UserTeam;
import com.example.demo.model.dto.TeamQuery;
import com.example.demo.model.request.*;
import com.example.demo.model.vo.UserTeamVO;
import com.example.demo.service.TeamService;
import com.example.demo.service.UserService;
import com.example.demo.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @className: TeamController
 * @author: ZH
 * @date: 2024/12/6 16:47
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request)
    {
        if(teamAddRequest==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        User loginUser = userService.getLoginUser(request);
        //TODO 校验合法
        long teamId = teamService.addTeam(team,loginUser);
        return ResultUtils.success(team.getId());
    }

/*    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(long id)
    {
        if(id<=0) throw new BusinessException(ErrorCode.NULL_ERROR);

        //TODO 校验合法
        boolean delete = teamService.removeById(id);
        if(!delete)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }*/

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest httpServletRequest)
    {
        if(teamQuitRequest==null||teamQuitRequest.getId()==null||teamQuitRequest.getId()<=0) throw new BusinessException(ErrorCode.NULL_ERROR);
        Team team =new Team();
        BeanUtils.copyProperties(teamQuitRequest,team);
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean quit = teamService.quitByTeamId(team,loginUser);
        if(!quit)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"退出失败");
        }
        return ResultUtils.success(true);
    }
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpServletRequest){
        if(deleteRequest==null||deleteRequest.getId()<=0) throw new BusinessException(ErrorCode.NULL_ERROR);
        Long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean delete = teamService.deleteTeam(id,loginUser);
        if(!delete) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        return ResultUtils.success(true);
    }
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request)
    {
        if(teamUpdateRequest==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,team);
        User loginUser =  userService.getLoginUser(request);
        boolean save = teamService.updateTeam(team,loginUser);
        if(!save)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long id){
        if(id<=0) throw new BusinessException(ErrorCode.NULL_ERROR);
        Team team = teamService.getById(id);
        if(team==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        return ResultUtils.success(team);
    }

/*    @GetMapping("/list")
    public BaseResponse<List<Team>> listTeam(TeamQuery teamQuery){
        if(teamQuery==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        Team team = new Team();
        try {
            BeanUtils.copyProperties(teamQuery,team);
        }catch (Exception e) {
            log.error("copyProperties error",e);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        List<Team> list = teamService.list(queryWrapper);
        return ResultUtils.success(list);
    }*/

    @GetMapping("/list")
    public BaseResponse<List<UserTeamVO>> listTeam(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        boolean isAdmin = userService.isAdmin(request);
        List<UserTeamVO> list = teamService.listUserTeamVO(teamQuery,isAdmin);
        return ResultUtils.success(list);
    }
    @GetMapping("/list/my/create")
    public BaseResponse<List<UserTeamVO>> myCreateTeamList(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<UserTeamVO> list = teamService.listUserTeamVO(teamQuery,true);
        return ResultUtils.success(list);
    }
    @GetMapping("/list/my/join")
    public BaseResponse<List<UserTeamVO>> myJoinTeamList(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam>queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam>userTeamList= userTeamService.list(queryWrapper);
        //取出不重复的teamID
        Map<Long,List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());

        teamQuery.setIdList(idList);
        List<UserTeamVO> list = teamService.myJoinTeam(teamQuery);
        return ResultUtils.success(list);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamPage(TeamQuery teamQuery){
        if(teamQuery==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        Team team = new Team();
        try {
            BeanUtils.copyProperties(teamQuery,team);
        }catch (Exception e) {
            log.error("copyProperties error",e);
        }

        Page<Team> page = new Page<Team>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> list = teamService.page(page, queryWrapper);
        return ResultUtils.success(list);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if(teamJoinRequest==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean res = teamService.joinTeam(teamJoinRequest,loginUser);

        return ResultUtils.success(res);
    }
}
