package com.github.thundax.common.exception;

public enum ErrorCode {
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源状态冲突"),
    SORT_EMPTY_INPUT(400, "排序输入不能为空"),
    SORT_DUPLICATE_ID(400, "排序实体存在重复 ID"),
    SORT_MISSING_ID(400, "排序实体集合与查询范围不一致"),
    SORT_CONCURRENT_MODIFICATION(409, "排序存在并发修改，请重试"),
    SORT_DB_FAILURE(500, "排序数据库异常"),
    SYSTEM_ERROR(500, "系统异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
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
