package com.example.demo.utils.commonUtils;

import com.example.demo.model.domain.Team;

import java.lang.reflect.Field;

/**
 * @className: Compare
 * @author: ZH
 * @date: 2024/12/12 19:36
 * @Version: 1.0
 * @description:比较两个类的关系
 */

public class Compare {

    /*
    * @Author: ZH
    * @Description:判断两个类是否为包含关系
    * @Date: 19:37 2024/12/12
    * @Param:ObjectA , ObjectB
    * @return:true 为A包含B false为A不包含B
    **/
    //TODO
    public static  boolean Contain(Object fa, Object ch){
        if(fa==null||ch==null||!fa.getClass().equals(ch.getClass())) return false;


        return false;
    }
}
