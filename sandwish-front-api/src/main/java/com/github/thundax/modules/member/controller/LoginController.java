package com.github.thundax.modules.member.controller;

import com.github.thundax.common.web.BaseFrontController;
import com.github.thundax.modules.member.assembler.MemberLoginInterfaceAssembler;
import com.github.thundax.modules.member.response.MemberLoginStatusResponse;
import com.github.thundax.modules.member.security.MemberSecurityContext;
import com.github.thundax.modules.member.security.MemberSpringPrincipal;
import com.github.thundax.modules.member.utils.YwtbProperties;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/auth")
public class LoginController extends BaseFrontController {

    @Value("${front.security.success-url}")
    private String successUrl;

    private final YwtbProperties properties;
    private final MemberLoginInterfaceAssembler memberLoginInterfaceAssembler;

    @Autowired
    public LoginController(YwtbProperties properties, MemberLoginInterfaceAssembler memberLoginInterfaceAssembler) {
        this.properties = properties;
        this.memberLoginInterfaceAssembler = memberLoginInterfaceAssembler;
    }

    // 第三方登录页面入口保留 Model 和视图跳转适配，不作为本轮核心 API 模型隔离目标。
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        MemberSpringPrincipal principal = MemberSecurityContext.getPrincipal();
        // 如果已经登录，则跳转到管理首页
        if (principal != null) {
            return "redirect:" + successUrl;
        }
        logger.info("登录方法进入");
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        String requestUrl = savedRequest != null ? savedRequest.getRedirectUrl() : successUrl;
        if (StringUtils.endsWith(requestUrl, "?")) {
            requestUrl = StringUtils.substringBeforeLast(requestUrl, "?");
        }
        logger.info("登录方法中的requestUrl: {}", requestUrl);
        String url =
                properties.getThirdLoginUrl() + URLEncoder.encode(properties.getLoginBackUrl() + requestUrl, "UTF-8");
        logger.info("登录方法中的url: {}", url);
        model.addAttribute("url", url);
        return "modules/member/login";
    }

    // 第三方登录失败页面入口保留 Model 和视图跳转适配，不作为本轮核心 API 模型隔离目标。
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public String loginFail(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        MemberSpringPrincipal principal = MemberSecurityContext.getPrincipal();

        // 如果已经登录，则跳转到管理首页
        if (principal != null) {
            return "redirect:" + successUrl;
        }
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        String requestUrl = savedRequest != null ? savedRequest.getRedirectUrl() : successUrl;
        String url =
                properties.getThirdLoginUrl() + URLEncoder.encode(properties.getLoginBackUrl() + requestUrl, "UTF-8");
        model.addAttribute("url", url);
        return "modules/member/login";
    }

    @RequestMapping("check-login")
    @ResponseBody
    public MemberLoginStatusResponse checkLogin() {
        return memberLoginInterfaceAssembler.toLoginStatusResponse(MemberSecurityContext.getPrincipal() != null);
    }
}
