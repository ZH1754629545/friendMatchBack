package com.example.demo.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @className: DeleteRequest
 * @author: ZH
 * @date: 2025/1/2 18:33
 * @Version: 1.0
 * @description:
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    protected Long id;
}
