package com.shanshan.auction.common;

public enum ResultCode {
    SUCCESS(200, "success"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    PARAM_ERROR(400, "参数错误"),
    SYSTEM_ERROR(500, "系统错误"),
    BUSINESS_ERROR(600, "业务错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
} 