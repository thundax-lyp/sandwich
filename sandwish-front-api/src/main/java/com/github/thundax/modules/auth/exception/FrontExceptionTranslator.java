package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.BizException;
import com.github.thundax.common.web.exception.ExceptionTranslator;
import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.exception.WebErrorCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class FrontExceptionTranslator implements ExceptionTranslator, Ordered {

    private static final Map<String, ApiError> API_ERRORS = new HashMap<String, ApiError>();

    static {
        register(WebErrorCode.BAD_REQUEST, "AUTH-00001", "auth.exception.invalid-captcha", "图形验证码错误");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00002", "auth.exception.invalid-sms-code", "短信验证码错误");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00003", "auth.exception.invalid-email-code", "邮箱验证码错误");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00004", "auth.exception.invalid-username-password", "用户名或密码错误");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00005", "auth.exception.refresh-token-expired", "refreshToken 已失效");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00006", "auth.exception.access-token-expired", "accessToken 已失效");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00007", "auth.exception.login-request-too-many", "登录请求过多");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00008", "auth.exception.login-form-key-expired", "登录表单密钥已失效");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00009", "auth.exception.login-form-expired", "登录表单已失效");
    }

    @Override
    public SandwishException translate(Exception exception) {
        if (!(exception instanceof BizException)) {
            return null;
        }
        BizException bizException = (BizException) exception;
        ApiError apiError = apiError(bizException);
        return new SandwishException(
                apiError.webErrorCode,
                apiError.code,
                apiError.messageKey,
                defaultMessage(bizException, apiError.defaultMessage),
                bizException.getMessageArgs());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private ApiError apiError(BizException exception) {
        if ("auth.exception.invalid-password".equals(exception.getMessageKey())) {
            return API_ERRORS.get("AUTH-00004");
        }
        ApiError apiError = API_ERRORS.get(exception.getCode());
        if (apiError != null) {
            return apiError;
        }
        if ("BIZ-00001".equals(exception.getCode())) {
            return new ApiError(
                    WebErrorCode.SYSTEM_ERROR,
                    WebErrorCode.SYSTEM_ERROR.getCode(),
                    WebErrorCode.SYSTEM_ERROR.getMessageKey(),
                    WebErrorCode.SYSTEM_ERROR.getMessage());
        }
        return new ApiError(
                WebErrorCode.BAD_REQUEST,
                WebErrorCode.BAD_REQUEST.getCode(),
                WebErrorCode.BAD_REQUEST.getMessageKey(),
                WebErrorCode.BAD_REQUEST.getMessage());
    }

    private String defaultMessage(BizException exception, String fallbackMessage) {
        return exception.getDefaultMessage() == null ? fallbackMessage : exception.getDefaultMessage();
    }

    private static void register(WebErrorCode webErrorCode, String code, String messageKey, String defaultMessage) {
        API_ERRORS.put(code, new ApiError(webErrorCode, code, messageKey, defaultMessage));
    }

    private static class ApiError {

        private final WebErrorCode webErrorCode;
        private final String code;
        private final String messageKey;
        private final String defaultMessage;

        ApiError(WebErrorCode webErrorCode, String code, String messageKey, String defaultMessage) {
            this.webErrorCode = webErrorCode;
            this.code = code;
            this.messageKey = messageKey;
            this.defaultMessage = defaultMessage;
        }
    }
}
