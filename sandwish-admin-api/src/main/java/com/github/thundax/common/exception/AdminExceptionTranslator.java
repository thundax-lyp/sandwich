package com.github.thundax.common.exception;

import com.github.thundax.common.web.exception.ExceptionTranslator;
import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.exception.WebErrorCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class AdminExceptionTranslator implements ExceptionTranslator, Ordered {

    private static final Map<String, ApiError> API_ERRORS = new HashMap<String, ApiError>();

    static {
        register(WebErrorCode.BAD_REQUEST, "AUTH-00001", "auth.exception.invalid-captcha", "验证码错误");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00002", "auth.exception.invalid-username-password", "用户名或密码错误");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00003", "auth.exception.invalid-password", "密码错误");
        register(WebErrorCode.FORBIDDEN, "AUTH-00004", "auth.exception.banned-account", "用户已禁用");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00005", "auth.exception.login-request-too-many", "登录请求过多");
        register(WebErrorCode.BAD_REQUEST, "AUTH-00006", "auth.exception.invalid-token", "token 已失效");
        register(
                WebErrorCode.SYSTEM_ERROR,
                "AUTH-00007",
                "auth.exception.oauth2-authorization-not-configured",
                "OAuth2 authorization 未配置");
        register(
                WebErrorCode.BAD_REQUEST,
                "AUTH-00008",
                "auth.exception.oauth2-grant-type-unsupported",
                "OAuth2 grant type unsupported");
        register(
                WebErrorCode.SYSTEM_ERROR,
                "AUTH-00009",
                "auth.exception.oauth2-client-not-configured",
                "OAuth2 client 未配置");
        register(
                WebErrorCode.BAD_REQUEST,
                "AUTH-00010",
                "auth.exception.oauth2-client-secret-invalid",
                "OAuth2 client secret invalid");
        register(
                WebErrorCode.BAD_REQUEST,
                "AUTH-00011",
                "auth.exception.oauth2-client-request-invalid",
                "OAuth2 client request invalid");
        register(WebErrorCode.SYSTEM_ERROR, "AUTH-00012", "auth.exception.wecom-login-not-configured", "企业微信登录未配置");
        register(WebErrorCode.SYSTEM_ERROR, "AUTH-00013", "auth.exception.github-login-not-configured", "GitHub 登录未配置");
        register(WebErrorCode.BAD_REQUEST, "SYS-00001", "sys.exception.invalid-parameter", "参数无效");
        register(WebErrorCode.NOT_FOUND, "SYS-00002", "sys.exception.object-not-found", "资源不存在");
        register(WebErrorCode.CONFLICT, "SYS-00003", "sys.exception.object-exists", "资源已存在");
        register(WebErrorCode.BAD_REQUEST, "SYS-00004", "sys.exception.move-tree-node", "树节点移动失败");
        register(WebErrorCode.BAD_REQUEST, "SYS-00005", "sys.exception.sort-empty-input", "排序输入不能为空");
        register(WebErrorCode.BAD_REQUEST, "SYS-00006", "sys.exception.sort-missing-id", "排序实体集合与查询范围不一致");
        register(WebErrorCode.BAD_REQUEST, "SYS-00007", "sys.exception.sort-duplicate-id", "排序实体存在重复 ID");
        register(WebErrorCode.CONFLICT, "SYS-00008", "sys.exception.sort-concurrent-modification", "排序存在并发修改，请重试");
        register(WebErrorCode.SYSTEM_ERROR, "SYS-00009", "sys.exception.sort-db-failure", "排序数据库异常");
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
        ApiError apiError = API_ERRORS.get(exception.getCode());
        if (apiError != null) {
            return apiError;
        }
        ApiError sortApiError = sortApiError(exception.getCode());
        if (sortApiError != null) {
            return sortApiError;
        }
        if (BizExceptionBoundaryTechnicalFailure.isTechnicalFailure(exception)) {
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

    private ApiError sortApiError(String code) {
        if ("SORT-00001".equals(code)) {
            return API_ERRORS.get("SYS-00005");
        }
        if ("SORT-00002".equals(code)) {
            return API_ERRORS.get("SYS-00007");
        }
        if ("SORT-00003".equals(code)) {
            return API_ERRORS.get("SYS-00006");
        }
        if ("SORT-00004".equals(code)) {
            return API_ERRORS.get("SYS-00008");
        }
        if ("SORT-00005".equals(code)) {
            return API_ERRORS.get("SYS-00009");
        }
        return null;
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

    private static class BizExceptionBoundaryTechnicalFailure {

        private static boolean isTechnicalFailure(BizException exception) {
            return "BIZ-00001".equals(exception.getCode());
        }
    }
}
