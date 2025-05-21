package com.example.demo.service;

import com.example.demo.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author ZH
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-11-08 14:31:29
*/
public interface UserService extends IService<User> {

    /*
    * @Author: ZH
    * @Description:根据用户传入的标签筛选 结果
    * @Date: 14:49 2024/11/8
    * @Param:tagNameList 用户传入的标签
    * @return:
    **/
    User getSafetyUser(User originUser);

    List<User> searchUsersByTags(List<String> tagNameList);

    long userRegister(String userAccount, String userPassword, String checkPassword);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    int userLogout(HttpServletRequest request);


    int updateUser(User user, User loginUser);

    boolean isAdmin(User user);

    User getLoginUser(HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    List<User> matchUsers(long num, User user);
}
