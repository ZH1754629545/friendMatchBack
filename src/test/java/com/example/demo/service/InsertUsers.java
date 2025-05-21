package com.example.demo.service;

import com.example.demo.model.domain.User;
import com.example.demo.utils.AddUser;
import com.example.demo.utils.MyCommonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

/**
 * @className: InsertUsers
 * @author: ZH
 * @date: 2024/11/22 20:00
 * @Version: 1.0
 * @description:
 */
@SpringBootTest
public class InsertUsers {
    @Resource
    private UserService userService;
    /*
    * @Author: ZH
    * @Description: 并发插入用户
    * @Date: 20:19 2024/11/22
    * @Param:
    * @return:
    **/

    @Test
    public void insertUsers(){
        Scanner sc = new Scanner(System.in);
        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();
        ForkJoinTask<Integer> task = new AddUser(1,1000);
        ForkJoinPool.commonPool().invoke(task);
        stopWatch1.stop();
        System.out.println(stopWatch1.getTotalTimeMillis());



 /*       StopWatch stopWatch =new StopWatch();
        final Long MAX_NUMBER = 100000L;
        stopWatch.start();
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for(int i=0;i<10;i++)
        {
            List<User> userList = Collections.synchronizedList(new ArrayList<>());

            //并发执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(()-> {
                    for (int j = 0; j < MAX_NUMBER; j++) {
                        User user = new User();
                        user.setUsername("zh" + j);
                        user.setUserAccount(Long.toString(MyCommonUtils.randNumber(10)));
                        user.setAvatarUrl("../../src/img/avatar/mari.jpg");
                        user.setGender((MyCommonUtils.randNumber(2) % 2 == 0 ? 1 : 0));
                        user.setUserPassword("123456789");
                        user.setPhone(Long.toString(MyCommonUtils.randNumber(10)));
                        user.setEmail(Long.toString(MyCommonUtils.randNumber(10)) + "@qq.com");
                        user.setUserStatus(0);
                        user.setIsDelete(0);
                        user.setUserRole(0);
                        user.setUserCode("123456789");
                        user.setTags("[]");
                        user.setProfile("这个人很懒");
                        userList.add(user);
                    }

                    userService.saveBatch(userList,10000);
                }
            );

            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());*/

    }

}
//5155
//37737