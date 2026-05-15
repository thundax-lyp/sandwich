package com.github.thundax.common.exception;

import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.exception.WebErrorCode;

public final class OpenApiResponseExceptions {

    private OpenApiResponseExceptions() {}

    public static SandwishException missingApiKey() {
        return auth(WebErrorCode.UNAUTHORIZED, "AUTH-00001", "auth.exception.missing-api-key", "缺少 API KEY");
    }

    public static SandwishException invalidApiKey() {
        return auth(WebErrorCode.UNAUTHORIZED, "AUTH-00002", "auth.exception.invalid-api-key", "API KEY 无效");
    }

    public static SandwishException openClientUnavailable() {
        return auth(
                WebErrorCode.UNAUTHORIZED, "AUTH-00003", "auth.exception.open-client-unavailable", "OpenClient 不可用");
    }

    public static SandwishException ipNotAllowed() {
        return auth(WebErrorCode.FORBIDDEN, "AUTH-00004", "auth.exception.ip-not-allowed", "IP 不在白名单内");
    }

    public static SandwishException invalidTimestamp() {
        return auth(WebErrorCode.UNAUTHORIZED, "AUTH-00005", "auth.exception.invalid-timestamp", "timestamp 无效");
    }

    public static SandwishException replayedNonce() {
        return auth(WebErrorCode.UNAUTHORIZED, "AUTH-00006", "auth.exception.replayed-nonce", "nonce 已使用");
    }

    public static SandwishException invalidContentSha256() {
        return auth(WebErrorCode.UNAUTHORIZED, "AUTH-00007", "auth.exception.invalid-content-sha256", "body 摘要无效");
    }

    public static SandwishException invalidSignature() {
        return auth(WebErrorCode.UNAUTHORIZED, "AUTH-00008", "auth.exception.invalid-signature", "签名无效");
    }

    public static SandwishException apiSecretNotConfigured() {
        return auth(
                WebErrorCode.SYSTEM_ERROR, "AUTH-00009", "auth.exception.api-secret-not-configured", "API SECRET 未配置");
    }

    public static SandwishException permissionDenied() {
        return new SandwishException(WebErrorCode.FORBIDDEN);
    }

    private static SandwishException auth(
            WebErrorCode webErrorCode, String code, String messageKey, String defaultMessage) {
        return new SandwishException(webErrorCode, code, messageKey, defaultMessage);
    }
}
