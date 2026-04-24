package com.github.thundax.modules.assist.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.modules.assist.api.query.SignatureQueryParam;
import com.github.thundax.modules.assist.api.vo.SignatureVo;
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
     * @param queryParam 查询参数
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "assist:signature:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "page", method = RequestMethod.POST)
    PageVo<SignatureVo> page(@RequestBody @ApiParam("查询参数") SignatureQueryParam queryParam) throws ApiException;


    /**
     * 更新
     *
     * @param vo 对象
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "校验", notes = "assist:signature:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("校验")
    @RequestMapping(value = "verify", method = RequestMethod.POST)
    Boolean verify(@RequestBody @ApiParam("对象") SignatureVo vo) throws ApiException;


    /**
     * 删除
     *
     * @param list 列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除", notes = "assist:signature:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    Boolean delete(@RequestBody @ApiParam("列表") List<SignatureVo> list) throws ApiException;

}
