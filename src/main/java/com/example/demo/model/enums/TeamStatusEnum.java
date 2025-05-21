package com.example.demo.model.enums;

/**
 * @className: TeamStatusEnum
 * @author: ZH
 * @date: 2024/12/6 18:02
 * @Version: 1.0
 * @description:
 */

public enum TeamStatusEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");
    private int value;
    private String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }
    public static TeamStatusEnum getEnumByValue(int value) {
        if(value<0) return null;
        for (TeamStatusEnum statusEnum : TeamStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
