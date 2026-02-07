package com.microsoft.commen;

import lombok.Getter;

@Getter
public enum ErrorCode {
    PARAM_ERROR(40000, "参数错误"),
    NO_AUTH(40100, "无权限"),
    SYSTEM_ERROR(50000, "服务器出错"),
    DATABASE_ERROR(50001, "数据库操作失败")
    ;

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
