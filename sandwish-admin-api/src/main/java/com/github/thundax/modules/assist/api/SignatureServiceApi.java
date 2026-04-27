package com.github.thundax.modules.assist.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.modules.assist.request.SignatureDeleteRequest;
import com.github.thundax.modules.assist.request.SignaturePageRequest;
import com.github.thundax.modules.assist.request.SignatureVerifyRequest;
import com.github.thundax.modules.assist.response.SignatureResponse;
import com.github.thundax.modules.assist.response.SignatureVerifyResponse;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author thundax
 */
@Api(tags = "08-04.辅助-签名与验签")
@SysLogger(module = {"辅助", "签名"})
@RequestMapping(value = "/api/assist/signature")
public interface SignatureServiceApi {

    /**
     * 获取列表
     *
     * @param request 签名分页查询请求
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "assist:signature:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "page", method = RequestMethod.POST)
    PageVo<SignatureResponse> page(@RequestBody @ApiParam("签名分页查询请求") SignaturePageRequest request) throws ApiException;


    /**
     * 更新
     *
     * @param request 签名验签请求
     * @return 签名验签响应
     * @throws ApiException API异常
     */
    @ApiOperation(value = "校验", notes = "assist:signature:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("校验")
    @RequestMapping(value = "verify", method = RequestMethod.POST)
    SignatureVerifyResponse verify(@RequestBody @ApiParam("签名验签请求") SignatureVerifyRequest request) throws ApiException;


    /**
     * 删除
     *
     * @param list 签名删除请求列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除", notes = "assist:signature:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    Boolean delete(@RequestBody @ApiParam("签名删除请求列表") List<SignatureDeleteRequest> list) throws ApiException;

}
