package com.example.demo.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.model.domain.User;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Time;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static jodd.util.ThreadUtil.sleep;

/**
 * @className: PreCacheJob
 * @author: ZH
 * @date: 2024/11/28 17:33
 * @Version: 1.0
 * @description:
 */
@Component
@Slf4j
public class PreCacheJob {


    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //重点用户更新
    private List<Long>importantUserIds = Arrays.asList(7L);

    /*
    * @Author: ZH
    * @Description: 定时任务corn表达式每天执行一次
    * @Date: 17:35 2024/11/28
    * @Param:
    * @return:
    **/
    @Scheduled(cron = "0 25 * * * *")
    public void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("zh:preCacheJob:doCache:lock");

        try {
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("定时任务执行");
//                sleep(1000000);
                importantUserIds.forEach(id->{

                    String strKey = String.format("user:recommend:%d",id);
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    QueryWrapper queryWrapper = new QueryWrapper<>();
                    Page<User> userList1 = (Page<User>) userService.page(new Page<>( 1,20),queryWrapper);
                    try {
                        valueOperations.set(strKey,userList1,300000, TimeUnit.MILLISECONDS);

                    }catch (Exception e) {
                        log.error("redis存储失败",e);
                    }
                });
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error:",e);
            throw new RuntimeException(e);
        }finally {
            //释放自己的锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
