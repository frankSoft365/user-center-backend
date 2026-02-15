package com.microsoft.model.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum UserRoleEnum {
    ADMIN_ROLE(1, "管理员"),
    DEFAULT_ROLE(0, "普通用户")
    ;
    private final Integer value;
    private final String name;

    UserRoleEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public static UserRoleEnum getEnumByValue(Integer value) {
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (Objects.equals(userRoleEnum.value, value)) {
                return userRoleEnum;
            }
        }
        return null;
    }

    public static UserRoleEnum getEnumByName(String name) {
        if (name == null) {
            return UserRoleEnum.DEFAULT_ROLE;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (Objects.equals(userRoleEnum.name, name)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
