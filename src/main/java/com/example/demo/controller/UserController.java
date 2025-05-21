package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.BaseResponse;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.ResultUtils;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.domain.User;
import com.example.demo.model.request.UserLoginRequest;
import com.example.demo.model.request.UserRegisterRequest;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.demo.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @className: UserController
 * @author: LXHYouth
 * @date: 2024/11/14 16:54
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;
    @GetMapping("/test")
    public String Test(){
        return "成功";
    }
    /**
    * @Author: ZH
    * @Description: 通过标签搜索用户列表
    * @Date: 14:14 2024/11/17
    * @Param: [tagNameList]
    * @return: com.example.demo.common.BaseResponse<java.util.List<com.example.demo.model.domain.User>>
     *
    **/
//    TODO 分页返回
    @GetMapping("/search/tags")
    public BaseResponse<List<User>>searchUsersByTags(@RequestParam(required = false ) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList))
        {
            throw new RuntimeException(new BusinessException(ErrorCode.PARAMS_ERROR));
        }
        List<User>userList = userService.searchUsersByTags(tagNameList);

        return ResultUtils.success(userList);
    }
    /*
    * @Author: ZH
    * @Description: 主页推荐
    * @Date: 17:18 2024/11/22
    * @Param:
    * @return:
    **/
    @GetMapping("/recommend")
    public BaseResponse<Page<User>>searchUser(long pageNum,long pageSize,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        String strKey = String.format("user:recommend:%d",loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userList = (Page<User>) valueOperations.get(strKey);
        if(userList!=null)
        {
            return ResultUtils.success(userList);
        }
        QueryWrapper queryWrapper = new QueryWrapper<>();
        Page<User> userList1 = (Page<User>) userService.page(new Page<>( pageNum,pageSize),queryWrapper);
        try {
            valueOperations.set(strKey,userList1,300000, TimeUnit.MILLISECONDS);

        }catch (Exception e) {
            log.error("redis存储失败",e);
        }
        return ResultUtils.success(userList1);
    }

    @PostMapping("/register")
    public BaseResponse<Long>userRegister(@RequestBody UserRegisterRequest userRegisterRequest)
    {
        if(userRegisterRequest==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword= userRegisterRequest.getCheckPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount,userPassword,checkPassword);
        return ResultUtils.success(result);
    }
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @GetMapping("/current")
    public BaseResponse<User> userCurrent(HttpServletRequest request){
        Object userObj =request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId =currentUser.getId();
        // 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }
    @PostMapping("/update")
    public BaseResponse<Integer> userUpdate(@RequestBody User user,HttpServletRequest request)
    {
        if(user==null||request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser  =userService.getLoginUser(request);
        int result = userService.updateUser(user,loginUser);
        return ResultUtils.success(result);
    }
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, user));
    }

}
