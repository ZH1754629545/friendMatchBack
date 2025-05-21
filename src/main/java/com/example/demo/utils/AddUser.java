package com.example.demo.utils;

import com.example.demo.model.domain.User;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RecursiveTask;
@Component
public class AddUser extends RecursiveTask<Integer> {

    @Resource
    private UserService userService;

    public static AddUser addUser;

    @PostConstruct
    public void init(){
        addUser = this;
    }
    static final int THRESHOLD = 500;
    int start;
    int end;
    public AddUser(){

    }
    public AddUser(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        if(start>end) return 0;
        if (end - start <= THRESHOLD) {
            List<User>userList = Collections.synchronizedList(new ArrayList<>());
            for(int i=0;i<=end-start;i++){
                User user = new User();
                user.setUsername("zh"+ start+i);
                user.setUserAccount(Long.toString(MyCommonUtils.randNumber(10)));
                user.setAvatarUrl("../../src/img/avatar/mari.jpg");
                user.setGender((MyCommonUtils.randNumber(2)%2==0?1:0));
                String encryptPassword = DigestUtils.md5DigestAsHex(("ZH" + "123456789").getBytes());
                user.setUserPassword(encryptPassword);
                user.setUserPassword("123456789");
                user.setPhone(Long.toString(MyCommonUtils.randNumber(10)));
                user.setEmail(Long.toString(MyCommonUtils.randNumber(10))+"@qq.com");
                user.setUserStatus(0);
                user.setIsDelete(0);
                user.setUserRole(0);
                user.setUserCode("123456789");
                user.setTags("[]");
                user.setProfile("这个人很懒");
                userList.add(user);
            }
            addUser.userService.saveBatch(userList, Math.max(userList.size()/10,1));
            return 1;
        }
        // 任务太大,一分为二:
        int middle = (end + start) / 2;
//        System.out.println(String.format("split %d~%d ==> %d~%d, %d~%d", start, end, start, middle, middle, end));
         AddUser addUser1= new AddUser(start, middle);
        AddUser addUser2 = new AddUser(middle+1, end);
        invokeAll(addUser1, addUser2);
        return 1;
    }
}