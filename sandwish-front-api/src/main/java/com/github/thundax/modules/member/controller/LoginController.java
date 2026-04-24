package com.github.thundax.modules.member.controller;

import com.github.thundax.common.web.BaseFrontController;
import com.github.thundax.modules.member.security.MemberAuthenticationToken;
import com.github.thundax.modules.member.security.MemberPrincipal;
import com.github.thundax.modules.member.utils.ShiroUtils;
import com.github.thundax.modules.member.utils.YwtbProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

/**
 * @author wdit
 */
@Controller
@RequestMapping(value = "/auth")
public class LoginController extends BaseFrontController {

    @Value("${shiro.successUrl}")
    private String successUrl;

    private final YwtbProperties properties;

    @Autowired
    public LoginController(YwtbProperties properties) {
        this.properties = properties;
    }


    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login(HttpServletRequest request,Model model) throws Exception {
        MemberPrincipal principal = ShiroUtils.getPrincipal();
        // 如果已经登录，则跳转到管理首页
        if (principal != null) {
            return "redirect:" + successUrl;
        }
        logger.info("登录方法进入");
        SavedRequest savedRequest = WebUtils.getSavedRequest(request);
        String requestUrl= savedRequest != null ? savedRequest.getRequestUrl() : successUrl;
        if(StringUtils.endsWith(requestUrl,"?")){
            requestUrl = StringUtils.substringBeforeLast(requestUrl,"?");
        }
        logger.info("登录方法中的requestUrl: {}",requestUrl);
        String url =  properties.getThirdLoginUrl() + URLEncoder.encode(properties.getLoginBackUrl() + requestUrl, "UTF-8");
        logger.info("登录方法中的url: {}",url);
        model.addAttribute("url",url);
        return "modules/member/login";
    }

    /**
     * 登录失败，真正登录的POST请求由Filter完成
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public String loginFail(HttpServletRequest request, Model model) throws Exception {
        MemberPrincipal principal = ShiroUtils.getPrincipal();

        // 如果已经登录，则跳转到管理首页
        if (principal != null) {
            return "redirect:" + successUrl;
        }
        SavedRequest savedRequest = WebUtils.getSavedRequest(request);
        String requestUrl = savedRequest != null ? savedRequest.getRequestUrl() : successUrl;
        String url =  properties.getThirdLoginUrl() + URLEncoder.encode(properties.getLoginBackUrl() + requestUrl, "UTF-8");
        model.addAttribute("url",url);
        return "modules/member/login";
    }

    @RequestMapping("check-login")
    @ResponseBody
    public String checkLogin(){
        return ShiroUtils.getPrincipal() == null ? "false" : "true";
    }


    @ResponseBody
    @RequestMapping("testlogin")
    public String testLogin(){
        //登录
        MemberAuthenticationToken token = new MemberAuthenticationToken();
        token.setUsername("4f379cdb-76c6-4fed-8455-ffc9f32e3aa0");
        // 自动登录的话，需要使用默认密码登录
        token.setPassword("7D@&wweR".toCharArray());
        ShiroUtils.getSubject().login(token);
        return "true";
    }

}
