package com.github.thundax.common.web.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

@ApiModel(value = "ApiResponse", description = "统一 API 响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> implements Serializable {

    public static final String SUCCESS_CODE = "COMMON-00000";
    public static final String ERROR_CODE = "COMMON-00006";
    public static final String SUCCESS_MESSAGE = "操作成功";
    public static final String ERROR_MESSAGE = "未知异常，请联系管理员";

    private String code;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(SUCCESS_CODE, message, data);
    }

    public static <T> ApiResponse<T> failure() {
        return new ApiResponse<>(ERROR_CODE, ERROR_MESSAGE);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(ERROR_CODE, message);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(code, message);
    }

    @ApiModelProperty(name = "code", value = "响应码，COMMON-00000 表示成功")
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @ApiModelProperty(name = "message", value = "响应消息")
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @ApiModelProperty(name = "data", value = "响应数据")
    @JsonProperty("data")
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
