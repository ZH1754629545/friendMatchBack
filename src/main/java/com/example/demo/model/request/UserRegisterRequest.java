package com.example.demo.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @className: UserRegisterRequest
 * @author: ZH
 * @date: 2024/11/18 18:34
 * @Version: 1.0
 * @description:
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serializableUID = 1L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

}
