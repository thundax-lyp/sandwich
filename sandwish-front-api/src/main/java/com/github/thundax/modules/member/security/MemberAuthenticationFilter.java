package com.github.thundax.modules.member.security;

import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.web.RequestUtils;
import com.github.thundax.modules.member.utils.RsaSessionUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wdit
 */
public class MemberAuthenticationFilter extends FormAuthenticationFilter {

    public static final String DEFAULT_CAPTCHA_PARAM = "validateCode";
    public static final String DEFAULT_MESSAGE_PARAM = "message";
    public static final String DEFAULT_CALLBACK_PARAM = "callback";

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String username = getUsername(request);
        String password = getPassword(request);
        boolean rememberMe = isRememberMe(request);
        String host = RequestUtils.getRemoteAddr((HttpServletRequest) request);
        String captcha = getCaptcha(request);

        return new MemberAuthenticationToken(username, password.toCharArray(), rememberMe, host, captcha);
    }

    public String getCaptchaParam() {
        return DEFAULT_CAPTCHA_PARAM;
    }

    protected String getCaptcha(ServletRequest request) {
        return WebUtils.getCleanParam(request, getCaptchaParam());
    }

    public String getMessageParam() {
        return DEFAULT_MESSAGE_PARAM;
    }

    /**
     * 登录成功之后跳转URL
     */
    @Override
    public String getSuccessUrl() {
        return super.getSuccessUrl();
    }

    @Override
    protected void issueSuccessRedirect(ServletRequest request, ServletResponse response)
            throws Exception {
        WebUtils.issueRedirect(request, response, getSuccessUrl(), null, true);
    }


    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        if (isLoginRequest(request, response)) {
            if (isLoginSubmission(request, response)) {
                return executeLogin(request, response);
            } else {
                // 放行 allow them to see the login page ;)
                return true;
            }
        } else {
            HttpServletRequest httpRequest = WebUtils.toHttp(request);

            if (isAjax(httpRequest)) {
                HttpServletResponse httpServletResponse = (HttpServletResponse)response;
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                saveRequestAndRedirectToLogin(request, response);
            }

            return false;
        }
    }

    /**
     * 判断ajax请求
     *
     * @param request
     * @return
     */
    boolean isAjax(HttpServletRequest request) {
        return (request.getHeader("X-Requested-With") != null && "XMLHttpRequest".equals(request.getHeader("X-Requested-With").toString()));
    }

    /**
     * 登录失败调用事件
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token,
                                     AuthenticationException e,
                                     ServletRequest request,
                                     ServletResponse response) {
        String className = e.getClass().getName(), message;
        if (IncorrectCredentialsException.class.getName().equals(className)
                || UnknownAccountException.class.getName().equals(className)) {
            message = "用户或密码错误, 请重试.";

        } else {
            message = "系统出现点问题，请稍后再试！";
            e.printStackTrace();
        }
        request.setAttribute(getFailureKeyAttribute(), className);
        request.setAttribute(getMessageParam(), message);
        return true;
    }

    @Override
    protected String getPassword(ServletRequest request) {
        String encryptedValue = super.getPassword(request);
        if (StringUtils.isEmpty(encryptedValue)) {
            return StringUtils.EMPTY;
        }
        return RsaSessionUtils.decryptRsaValue((HttpServletRequest) request, encryptedValue);
    }

}
