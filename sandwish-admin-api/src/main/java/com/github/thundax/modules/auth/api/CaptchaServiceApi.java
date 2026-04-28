package com.github.thundax.modules.auth.api;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.request.CaptchaRefreshRequest;
import com.github.thundax.modules.auth.response.CaptchaRefreshResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "01-02. 鉴权-图形验证码")
@RequestMapping(value = "/api/auth")
public interface CaptchaServiceApi {

    /**
     * 图形验证码
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IO异常
     */
    @ApiOperation(value = "图形验证码", notes = "ignore")
    @GetMapping(value = "captcha")
    void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 刷新图形验证码
     *
     * @param request 图形验证码刷新请求
     * @return 成功:true
     * @throws ApiException API异常
     */
    @ApiOperation(value = "刷新图形验证码", notes = "ignore")
    @PostMapping(value = "captcha/refresh")
    CaptchaRefreshResponse refreshCaptcha(@RequestBody CaptchaRefreshRequest request) throws ApiException;
}
