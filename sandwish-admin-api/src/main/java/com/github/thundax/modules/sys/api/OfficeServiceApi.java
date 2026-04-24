package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.query.MoveTreeNodeQueryParam;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.api.query.OfficeQueryParam;
import com.github.thundax.modules.sys.api.vo.OfficeVo;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author wdit
 */
@Api(tags = "02-02.系统-组织机构")
@SysLogger(module = {"系统", "组织机构"})
@RequestMapping(value = "/api/sys/office")
public interface OfficeServiceApi {

    /**
     * 获取对象
     *
     * @param office 组织机构
     * @return 组织机构
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取对象", notes = "sys:office:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    OfficeVo get(@RequestBody @ApiParam("组织机构") OfficeVo office) throws ApiException;


    /**
     * 获取列表
     *
     * @param queryParam 组织机构查询参数
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "sys:office:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    List<OfficeVo> list(@RequestBody @ApiParam("组织机构查询参数") OfficeQueryParam queryParam) throws ApiException;


    /**
     * 添加
     *
     * @param office 组织机构
     * @return 组织机构
     * @throws ApiException API异常
     */
    @ApiOperation(value = "添加", notes = "sys:office:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    OfficeVo add(@RequestBody @ApiParam(value = "组织机构") OfficeVo office) throws ApiException;


    /**
     * 更新
     *
     * @param office 组织机构
     * @return 组织机构
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新", notes = "sys:office:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    OfficeVo update(@RequestBody @ApiParam("组织机构") OfficeVo office) throws ApiException;


    /**
     * 删除
     *
     * @param list 列表
     * @return 影响记录数
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除", notes = "sys:office:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    Boolean delete(@RequestBody @ApiParam("组织机构列表") List<OfficeVo> list) throws ApiException;


    /**
     * 获取树形结构
     *
     * @param excludeList 排除列表
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "super")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "tree", method = RequestMethod.POST)
    List<OfficeVo> tree(@RequestBody @ApiParam("排除列表") List<OfficeVo> excludeList) throws ApiException;


    /**
     * 移动
     *
     * @param queryParam 移动参数
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "移动", notes = "sys:office:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("移动")
    @RequestMapping(value = "move", method = RequestMethod.POST)
    Boolean move(@RequestBody @ApiParam("移动参数") MoveTreeNodeQueryParam queryParam) throws ApiException;

}
