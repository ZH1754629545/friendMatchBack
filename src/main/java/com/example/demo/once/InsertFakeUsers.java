package com.example.demo.once;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.domain.User;
import com.example.demo.utils.MyCommonUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @className: InsertFakeUsers
 * @author: ZH
 * @date: 2024/11/22 19:07
 * @Version: 1.0
 * @description:
 */
@Component
public class InsertFakeUsers {
    @Resource
    private UserMapper userMapper;





    /*
    * @Author: ZH
    * @Description:批量插入用户
    * @Date: 19:07 2024/11/22
    * @Param:
    * @return:
    **/
// 加注解就可以每隔5s执行一次
//    @Scheduled(fixedDelay = 5000)
    public void insertUsers(){
        StopWatch stopWatch =new StopWatch();
        final Long MAX_NUMBER = 1000L;
        stopWatch.start();
        for (int i = 0; i < MAX_NUMBER; i++) {
            User user = new User();
            user.setUsername("zh"+ Long.toString(MyCommonUtils.randNumber(3)));
            user.setUserAccount(Long.toString(MyCommonUtils.randNumber(10)));
            user.setAvatarUrl("../../src/img/avatar/mari.jpg");
            user.setGender((MyCommonUtils.randNumber(2)%2==0?1:0));
            user.setUserPassword("123456789");
            user.setPhone(Long.toString(MyCommonUtils.randNumber(10)));
            user.setEmail(Long.toString(MyCommonUtils.randNumber(10))+"@qq.com");
            user.setUserStatus(0);
            user.setIsDelete(0);
            user.setUserRole(0);
            user.setUserCode("123456789");
            user.setTags("[]");
            user.setProfile("这个人很懒");
            userMapper.insert(user);
        }

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }
}


