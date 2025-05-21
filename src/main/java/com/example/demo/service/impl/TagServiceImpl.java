package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.model.domain.Tag;
import com.example.demo.service.TagService;
import com.example.demo.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author ZH
* @description 针对表【tag】的数据库操作Service实现
* @createDate 2024-11-08 14:29:26
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




