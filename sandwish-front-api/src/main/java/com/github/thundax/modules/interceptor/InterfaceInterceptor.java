package com.github.thundax.modules.interceptor;

import com.github.thundax.common.exception.WditException;
import com.github.thundax.common.utils.encrypt.Md5;
import com.github.thundax.modules.annotation.ApiLogin;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class InterfaceInterceptor extends HandlerInterceptorAdapter {

    @Value("${wdit.interface.authCode}")
    private String authCode;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        ApiLogin annotation;
        if (handler instanceof HandlerMethod) {
            annotation = ((HandlerMethod) handler).getMethodAnnotation(ApiLogin.class);
        } else {
            return true;
        }

        if (annotation == null) {
            return true;
        }

        String nonce = request.getHeader("nonce");
        String sign = request.getHeader("sign");
        if (StringUtils.isEmpty(nonce) || StringUtils.isEmpty(sign)) {
            throw new WditException("非法请求");
        }
        // 判断请求是否在五分钟之内
        if (!validateDate(Long.parseLong(nonce))) {
            throw new WditException("请求已过时");
        }

        String encode = encode(authCode, nonce);
        if (!StringUtils.equals(encode, sign)) {
            throw new WditException("非法请求");
        }
        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Object o,
            ModelAndView modelAndView)
            throws Exception {}

    @Override
    public void afterCompletion(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Object o,
            Exception e)
            throws Exception {}

    private boolean validateDate(Long timestamp) {
        if (timestamp == null) {
            return false;
        }
        return System.currentTimeMillis() - timestamp < 30 * 60 * 1000;
    }

    private String encode(String appSecret, String nonce) {
        return Md5.encrypt(nonce + appSecret);
    }

    public static void main(String[] args) {

        String s = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println(s);
        // AppZiXunMailController interfaceInterceptor = new AppZiXunMailController();
        String aaa = "77dddd6a15444710a0a51defe66545f7";
        long time = new Date().getTime();
        System.out.println(time);
        String encrypt = Md5.encrypt(time + aaa);
        System.out.println(encrypt);
    }
}
