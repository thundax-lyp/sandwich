package com.github.thundax.common.web;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 *
 */
public class ReturnObject extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;


	public ReturnObject() {
		put("code", 0);
		put("message", "操作成功");
	}

	public static ReturnObject error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}

	public static ReturnObject error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}

	public static ReturnObject error(int code, String msg) {
		ReturnObject returnObject = new ReturnObject();
		returnObject.put("code", code);
		returnObject.put("message", msg);
		return returnObject;
	}

	public static ReturnObject ok(String msg) {
		ReturnObject returnObject = new ReturnObject();
		returnObject.put("message", msg);
		return returnObject;
	}

	public static ReturnObject ok(Map<String, Object> map) {
		ReturnObject returnObject = new ReturnObject();
		returnObject.putAll(map);
		return returnObject;
	}

	public static ReturnObject ok() {
		return new ReturnObject();
	}

	public ReturnObject put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}
