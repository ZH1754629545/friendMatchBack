package com.example.demo.mapper;

import com.example.demo.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.GetMapping;

/**
* @author ZH
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-11-08 14:31:29
* @Entity com.example.demo.model.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {
}




