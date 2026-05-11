package com.github.thundax.common.web.exception;

public enum WebErrorCode {
    BAD_REQUEST("COMMON-00001", 400, "请求参数错误"),
    UNAUTHORIZED("COMMON-00002", 401, "未认证"),
    FORBIDDEN("COMMON-00003", 403, "无访问权限"),
    NOT_FOUND("COMMON-00004", 404, "资源不存在"),
    CONFLICT("COMMON-00005", 409, "资源状态冲突"),
    SYSTEM_ERROR("COMMON-00006", 500, "系统异常");

    private final String code;
    private final int httpStatus;
    private final String message;

    WebErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
