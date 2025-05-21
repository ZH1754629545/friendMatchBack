package com.example.demo.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @className: TeamPageRequest
 * @author: ZH
 * @date: 2024/12/6 17:19
 * @Version: 1.0
 * @description:
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /*
    * @Author: ZH
    * @Description: 页面大小
    * @Date: 17:20 2024/12/6
    * @Param:
    * @return:
    **/

    protected int pageSize = 10;
    /*
    * @Author: ZH
    * @Description:当前页面
    * @Date: 17:20 2024/12/6
    * @Param:
    * @return:
    **/

    protected int pageNum = 1;

}
