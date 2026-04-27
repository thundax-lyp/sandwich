package com.github.thundax.modules.assist.plugins.koal.sign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;
import java.io.Serializable;

/**
 * 签名验签响应基类
 *
 * @author wdit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponse implements Serializable {

    public static final String SUCCESS_CODE = "0";

    /** 消息编码 * */
    private String errorCode;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public static boolean isSuccess(BaseResponse response) {
        return response != null
                && StringUtils.equalsIgnoreCase(response.getErrorCode(), SUCCESS_CODE);
    }
}
