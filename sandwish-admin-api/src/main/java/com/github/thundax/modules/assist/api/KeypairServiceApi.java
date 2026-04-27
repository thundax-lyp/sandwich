package com.github.thundax.modules.assist.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.assist.request.KeypairPublicKeyRequest;
import com.github.thundax.modules.assist.response.KeypairPublicKeyResponse;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/** @author wdit */
@Api(tags = "08-05.辅助-公钥与私钥")
@SysLogger(module = {"辅助", "公私钥对"})
@RequestMapping(value = "/api/assist/keypair")
public interface KeypairServiceApi {

    /**
     * 获取公钥
     *
     * @param request 公钥获取请求
     * @return 公钥
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取公钥", notes = "")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("获取公钥")
    @RequestMapping(value = "public", method = RequestMethod.POST)
    KeypairPublicKeyResponse publicKey(@RequestBody KeypairPublicKeyRequest request)
            throws ApiException;
}
