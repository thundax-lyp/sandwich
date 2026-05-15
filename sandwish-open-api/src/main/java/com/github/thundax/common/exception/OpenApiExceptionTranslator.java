package com.github.thundax.common.exception;

import com.github.thundax.common.web.exception.ExceptionTranslator;
import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.exception.WebErrorCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class OpenApiExceptionTranslator implements ExceptionTranslator, Ordered {

    private static final Map<String, ApiError> API_ERRORS = new HashMap<String, ApiError>();

    static {
        register(WebErrorCode.BAD_REQUEST, "SUBMISSION-00001", "submission.exception.invalid-parameter", "提交内容参数无效");
        register(WebErrorCode.BAD_REQUEST, "STORAGE-00001", "storage.exception.invalid-file", "文件无效");
    }

    @Override
    public SandwishException translate(Exception exception) {
        if (exception instanceof AccessDeniedException) {
            return OpenApiResponseExceptions.permissionDenied();
        }
        if (!(exception instanceof BizException)) {
            return null;
        }
        BizException bizException = (BizException) exception;
        ApiError apiError = API_ERRORS.get(bizException.getCode());
        if (apiError == null) {
            return new SandwishException(
                    WebErrorCode.BAD_REQUEST,
                    WebErrorCode.BAD_REQUEST.getCode(),
                    WebErrorCode.BAD_REQUEST.getMessageKey(),
                    defaultMessage(bizException, WebErrorCode.BAD_REQUEST.getMessage()),
                    bizException.getMessageArgs());
        }
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
