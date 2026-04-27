package com.github.thundax.modules.member.controller;

import com.github.thundax.modules.member.utils.YwtbProperties;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author zhangwei
 * @date 2021/08/23/16:35
 */
@Controller
@RequestMapping(value = "/auth")
public class LogoutController {

    @Value("${shiro.successUrl}")
    private String successUrl;

    private final YwtbProperties properties;

    @Autowired
    public LogoutController(YwtbProperties properties) {
        this.properties = properties;
    }

    @RequestMapping("logout")
    public String logout(HttpServletRequest request, HttpServletResponse response)
            throws UnsupportedEncodingException {
        try {
            Subject subject = SecurityUtils.getSubject();
            subject.logout();
            request.getSession().invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:"
                + properties.getLogoutUrl()
                + URLEncoder.encode(properties.getLoginBackUrl() + successUrl, "UTF-8");
    }
}
