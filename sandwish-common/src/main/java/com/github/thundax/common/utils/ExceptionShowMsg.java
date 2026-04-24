package com.github.thundax.common.utils;

/**
 * 用于信息弹出的异常类
 */
public class ExceptionShowMsg extends Exception {

    public ExceptionShowMsg(String msg){
        super(msg);
        this.msg=msg;

    }

    private String msg="";

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
