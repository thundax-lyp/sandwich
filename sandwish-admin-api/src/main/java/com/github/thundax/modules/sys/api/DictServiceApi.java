package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.api.query.DictQueryParam;
import com.github.thundax.modules.sys.api.vo.DictVo;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author thundax
 */
@Api(tags = "02-05.系统-字典")
@SysLogger(module = {"系统", "字典"})
@RequestMapping(value = "/api/sys/dict")
public interface DictServiceApi {

    /**
     * 获取对象
     *
     * @param dict 字典
     * @return 字典
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取对象", notes = "sys:dict:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    DictVo get(@RequestBody @ApiParam("字典") DictVo dict) throws ApiException;


    /**
     * 获取列表
     *
     * @param queryParam 字典查询参数
     * @return 字典列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "sys:dict:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    List<DictVo> list(@RequestBody @ApiParam("字典查询参数") DictQueryParam queryParam) throws ApiException;


    /**
     * 获取分页列表
     *
     * @param queryParam 字典查询参数
     * @return 分页
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取分页列表", notes = "sys:dict:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("分页")
    @RequestMapping(value = "page", method = RequestMethod.POST)
    PageVo<DictVo> page(@RequestBody @ApiParam("字典查询参数") DictQueryParam queryParam) throws ApiException;


    /**
     * 添加
     *
     * @param dict 字典
     * @return 字典
     * @throws ApiException API异常
     */
    @ApiOperation(value = "添加", notes = "sys:dict:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    DictVo add(@RequestBody @ApiParam("字典") DictVo dict) throws ApiException;


    /**
     * 更新
     *
     * @param dict 字典
     * @return 字典
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新", notes = "sys:dict:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    DictVo update(@RequestBody @ApiParam("字典") DictVo dict) throws ApiException;

    /**
     * 删除
     *
     * @param list 字典列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除", notes = "sys:dict:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    Boolean delete(@RequestBody @ApiParam("字典列表") List<DictVo> list) throws ApiException;


}
