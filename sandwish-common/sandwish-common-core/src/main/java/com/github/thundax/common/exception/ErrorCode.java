package com.github.thundax.common.exception;

public enum ErrorCode {
    SORT_EMPTY_INPUT("COMMON-00001", "排序输入不能为空"),
    SORT_DUPLICATE_ID("COMMON-00002", "排序实体存在重复 ID"),
    SORT_MISSING_ID("COMMON-00003", "排序实体集合与查询范围不一致"),
    SORT_CONCURRENT_MODIFICATION("COMMON-00004", "排序存在并发修改，请重试"),
    SORT_DB_FAILURE("COMMON-00005", "排序数据库异常");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
