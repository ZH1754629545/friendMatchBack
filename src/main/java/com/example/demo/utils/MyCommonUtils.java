package com.example.demo.utils;

import com.example.demo.exception.BusinessException;
import com.github.xiaoymin.knife4j.core.util.CommonUtils;
import com.sun.javafx.util.Utils;

import javax.rmi.CORBA.Util;
import java.lang.reflect.Field;
import java.util.Random;

/**
 * @className: CommonUtils
 * @author: ZH
 * @date: 2024/11/21 18:28
 * @Version: 1.0
 * @description:
 */

public class MyCommonUtils extends CommonUtils {
    /*
    * @Author: ZH
    * @Description: 判断两个相同的类是否为包含关系
    * @Date: 18:30 2024/11/21
    * @Param:
    * @return: bool
    **/

    public static boolean isContainClass (Object a,Object b){
        if(a==null|| b ==null){
            return false;
        }
        // 检查两个对象是否为相同类的实例
        if (a.getClass() != b.getClass()) {
            return true; // 如果它们不是相同类的实例，则返回true表示它们不同
        }
        // 获取对象的类
        Class<?> clazz = a.getClass();
        // 获取类的所有字段（包括私有字段）
        Field[] fields = clazz.getDeclaredFields();

        // 遍历所有字段
        for (Field field : fields) {
            field.setAccessible(true); // 设置字段为可访问

            try {
                // 获取两个对象中对应字段的值
                Object value1 = field.get(a);
                Object value2 = field.get(b);

                // 如果两个字段的值都不为null，并且它们不相等，则返回true表示它们不同
                if (value1 != null && value2 != null && !value1.equals(value2)) {
                    return true;
                }

            } catch (IllegalAccessException e) {
                // 如果访问字段时发生异常，则抛出运行时异常
                throw new RuntimeException(e);
            }
        }

        // 如果所有字段都检查完毕，并且没有找到不同的字段，则返回false表示它们相同
        return false;
    }
    /*
    * @Author: ZH
    * @Description: 按位随机数
    * @Date: 19:24 2024/11/22
    * @Param:
    * @return:
    **/

    public static Long randNumber(int length){
        if(length>10){
            throw new RuntimeException("长度不能超过10");
        }
        return (long)new Random().nextInt((int) Math.pow(10,length)) +(long)Math.pow(10L,length-1);
    }

}
