package com.example.demo.mapper;

import com.example.demo.model.domain.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author ZH
* @description 针对表【tag】的数据库操作Mapper
* @createDate 2024-11-08 14:29:26
* @Entity com.example.demo.model.domain.Tag
*/
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

}




