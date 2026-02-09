package com.microsoft.commen;

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
}
