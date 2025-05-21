package com.example.demo.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.demo.model.request.PageRequest;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * @className: TeamQuery
 * @author: ZH
 * @date: 2024/12/6 17:08
 * @Version: 1.0
 * @description:
 */
@Data
public class TeamQuery extends PageRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     *
     */
    private List<Long> IdList;
    /**
     * 描述
     */
    private String description;
    /**
     * 搜索文本
     */
    private String searchText;
    /**
     * 最大人数
     */
    private Integer maxNum;


    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

}
