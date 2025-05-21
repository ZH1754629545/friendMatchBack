package com.example.demo.service;
import com.example.demo.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @className: UserServiceTest
 * @author: LXHYouth
 * @date: 2024/11/8 15:31
 * @Version: 1.0
 * @description:
 */
@SpringBootTest

public class UserServiceTest {
    @Resource
    private UserService userService;


    @Test
    public void testSearchUsersByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assert.assertNotNull(userList);
    }
    @Test
    public void testB(){

    }
}
