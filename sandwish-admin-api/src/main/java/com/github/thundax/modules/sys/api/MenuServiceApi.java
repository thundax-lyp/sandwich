package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.request.MenuDisplayRequest;
import com.github.thundax.modules.sys.request.MenuIdRequest;
import com.github.thundax.modules.sys.request.MenuMoveRequest;
import com.github.thundax.modules.sys.request.MenuQueryRequest;
import com.github.thundax.modules.sys.request.MenuSaveRequest;
import com.github.thundax.modules.sys.response.MenuResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "02-03.系统-菜单")
@SysLogger(module = {"系统", "菜单"})
@RequestMapping(value = "/api/sys/menu")
public interface MenuServiceApi {

    /**
     * 获取对象
     *
     * @param request 菜单标识请求
     * @return 菜单
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取对象", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    MenuResponse get(@RequestBody @ApiParam("菜单标识请求") MenuIdRequest request) throws ApiException;

    /**
     * 获取列表
     *
     * @param request 菜单查询请求
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    List<MenuResponse> list(@RequestBody @ApiParam("菜单查询请求") MenuQueryRequest request) throws ApiException;

    /**
     * 添加
     *
     * @param request 菜单保存请求
     * @return 菜单
     * @throws ApiException API异常
     */
    @ApiOperation(value = "添加", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    MenuResponse add(@RequestBody @ApiParam(value = "菜单保存请求") MenuSaveRequest request) throws ApiException;

    /**
     * 更新
     *
     * @param request 菜单保存请求
     * @return 菜单
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("修改")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    MenuResponse update(@RequestBody @ApiParam("菜单保存请求") MenuSaveRequest request) throws ApiException;

    /**
     * 显示/隐藏
     *
     * @param list 菜单显示状态请求列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "显示/隐藏", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("显示")
    @RequestMapping(value = "display", method = RequestMethod.POST)
    Boolean updateDisplayFlag(@RequestBody @ApiParam("菜单显示状态请求列表") List<MenuDisplayRequest> list) throws ApiException;

    /**
     * 删除
     *
     * @param list 菜单标识请求列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    Boolean delete(@RequestBody @ApiParam("菜单标识请求列表") List<MenuIdRequest> list) throws ApiException;

    /**
     * 获取树形结构
     *
     * @param excludeList 排除菜单标识请求列表
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "tree", method = RequestMethod.POST)
    List<MenuResponse> tree(@RequestBody @ApiParam("排除菜单标识请求列表") List<MenuIdRequest> excludeList) throws ApiException;

    /**
     * 移动
     *
     * @param request 菜单树节点移动请求
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "排序", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("排序")
    @RequestMapping(value = "move", method = RequestMethod.POST)
    Boolean move(@RequestBody @ApiParam("菜单树节点移动请求") MenuMoveRequest request) throws ApiException;
}
