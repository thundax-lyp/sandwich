package com.github.thundax.common.exception;

import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.exception.SystemException;
import com.github.thundax.common.web.exception.WebErrorCode;

public final class AdminResponseExceptions {

    private AdminResponseExceptions() {}

    public static SandwishException invalidParameter(String name) {
        return new SandwishException(
                WebErrorCode.BAD_REQUEST, "SYS-00001", "sys.exception.invalid-parameter", "无效的参数: " + name);
    }

    public static SandwishException invalidToken() {
        return new SandwishException(
                WebErrorCode.BAD_REQUEST, "AUTH-00006", "auth.exception.invalid-token", "token 已失效");
    }

    public static SandwishException loginRequestTooMany() {
        return new SandwishException(
                WebErrorCode.BAD_REQUEST, "AUTH-00005", "auth.exception.login-request-too-many", "登录请求过多");
    }

    public static SandwishException invalidUsernamePassword() {
        return new SandwishException(
                WebErrorCode.BAD_REQUEST, "AUTH-00002", "auth.exception.invalid-username-password", "用户名或密码错误");
    }

    public static SandwishException bannedAccount() {
        return new SandwishException(WebErrorCode.FORBIDDEN, "AUTH-00004", "auth.exception.banned-account", "用户已禁用");
    }

    public static SandwishException objectNotFound() {
        return new SandwishException(WebErrorCode.NOT_FOUND, "SYS-00002", "sys.exception.object-not-found", "资源不存在");
    }

    public static SandwishException objectExists() {
        return new SandwishException(WebErrorCode.CONFLICT, "SYS-00003", "sys.exception.object-exists", "资源已存在");
    }

    public static SandwishException moveTreeNode() {
        return new SandwishException(WebErrorCode.BAD_REQUEST, "SYS-00004", "sys.exception.move-tree-node", "树节点移动失败");
    }

    public static SandwishException permissionDenied() {
        return new SandwishException(WebErrorCode.FORBIDDEN);
    }

    public static SandwishException oauth2AuthorizationNotConfigured() {
        return new SandwishException(
                WebErrorCode.SYSTEM_ERROR,
                "AUTH-00007",
                "auth.exception.oauth2-authorization-not-configured",
                "OAuth2 authorization 未配置");
    }

    public static SandwishException oauth2GrantTypeUnsupported() {
        return new SandwishException(
                WebErrorCode.BAD_REQUEST,
                "AUTH-00008",
                "auth.exception.oauth2-grant-type-unsupported",
                "OAuth2 grant type unsupported");
    }

    public static SandwishException oauth2ClientNotConfigured() {
        return new SandwishException(
                WebErrorCode.SYSTEM_ERROR,
                "AUTH-00009",
                "auth.exception.oauth2-client-not-configured",
                "OAuth2 client 未配置");
    }

    public static SandwishException oauth2ClientSecretInvalid() {
        return new SandwishException(
                WebErrorCode.BAD_REQUEST,
                "AUTH-00010",
                "auth.exception.oauth2-client-secret-invalid",
                "OAuth2 client secret invalid");
    }

    public static SandwishException oauth2ClientRequestInvalid() {
        return new SandwishException(
                WebErrorCode.BAD_REQUEST,
                "AUTH-00011",
                "auth.exception.oauth2-client-request-invalid",
                "OAuth2 client request invalid");
    }

    public static SandwishException wecomLoginNotConfigured() {
        return new SandwishException(
                WebErrorCode.SYSTEM_ERROR, "AUTH-00012", "auth.exception.wecom-login-not-configured", "企业微信登录未配置");
    }

    public static SandwishException githubLoginNotConfigured() {
        return new SandwishException(
                WebErrorCode.SYSTEM_ERROR, "AUTH-00013", "auth.exception.github-login-not-configured", "GitHub 登录未配置");
    }

    public static SandwishException system(String message) {
        return new SystemException(message);
    }
}
