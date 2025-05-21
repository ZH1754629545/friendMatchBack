package com.example.demo.service;

import com.example.demo.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.domain.User;
import com.example.demo.model.dto.TeamQuery;
import com.example.demo.model.request.TeamJoinRequest;
import com.example.demo.model.vo.UserTeamVO;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-12-06 16:41:07
*/
public interface TeamService extends IService<Team> {
    long addTeam(Team team, User loginUser);

    /*
    * @Author: ZH
    * @Description:搜索队伍
    * @Date: 16:41 2024/12/8
    * @Param:
    * @return:
    **/
    List<UserTeamVO> listUserTeamVO(TeamQuery teamQuery, Boolean isAdmin);

    List<UserTeamVO> myJoinTeam(TeamQuery teamQuery);

    boolean updateTeam(Team team,User loginUser);

    boolean quitByTeamId(Team team, User loginUser);

    boolean deleteTeam(Long id, User loginUser);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);
}
