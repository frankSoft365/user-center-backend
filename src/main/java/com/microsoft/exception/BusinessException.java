package com.microsoft.exception;

import com.microsoft.commen.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final String description;
    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
}
