package com.github.thundax.common.utils;

/**
 * React 前台输出类
 */
public class ReactResponseUtils {
    private int code;
    private String message;
    private Object data=new Object();

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


}
