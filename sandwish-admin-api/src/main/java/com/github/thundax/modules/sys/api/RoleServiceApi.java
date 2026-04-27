package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.request.RoleAssignUserRequest;
import com.github.thundax.modules.sys.request.RoleIdRequest;
import com.github.thundax.modules.sys.request.RolePriorityRequest;
import com.github.thundax.modules.sys.request.RoleQueryRequest;
import com.github.thundax.modules.sys.request.RoleSaveRequest;
import com.github.thundax.modules.sys.request.RoleStatusRequest;
import com.github.thundax.modules.sys.response.RoleMenuResponse;
import com.github.thundax.modules.sys.response.RoleResponse;
import com.github.thundax.modules.sys.response.RoleUserResponse;
import com.github.thundax.modules.sys.response.RoleUserTreeNodeResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/** @author wdit */
@Api(tags = "02-04.系统-权限")
@SysLogger(module = {"系统", "权限"})
@RequestMapping(value = "/api/sys/role")
public interface RoleServiceApi {

    /**
     * 获取对象
     *
     * @param request 角色标识请求
     * @return 权限
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取对象", notes = "sys:role:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    RoleResponse get(@RequestBody @ApiParam("角色标识请求") RoleIdRequest request) throws ApiException;

    /**
     * 获取列表
     *
     * @param request 角色查询请求
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "sys:role:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    List<RoleResponse> list(@RequestBody @ApiParam("角色查询请求") RoleQueryRequest request)
            throws ApiException;

    /**
     * 添加
     *
     * @param request 角色保存请求
     * @return 权限
     * @throws ApiException API异常
     */
    @ApiOperation(value = "添加", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    RoleResponse add(@RequestBody @ApiParam(value = "角色保存请求") RoleSaveRequest request)
            throws ApiException;

    /**
     * 更新
     *
     * @param request 角色保存请求
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    RoleResponse update(@RequestBody @ApiParam("角色保存请求") RoleSaveRequest request)
            throws ApiException;

    /**
     * 启用/禁用
     *
     * @param list 角色状态请求列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "启用/禁用", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("启用")
    @RequestMapping(value = "enable", method = RequestMethod.POST)
    Boolean updateEnableFlag(@RequestBody @ApiParam("角色状态请求列表") List<RoleStatusRequest> list)
            throws ApiException;

    /**
     * 排序
     *
     * @param list 角色排序请求列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "排序", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("排序")
    @RequestMapping(value = "priority", method = RequestMethod.POST)
    Boolean updatePriority(@RequestBody @ApiParam("角色排序请求列表") List<RolePriorityRequest> list)
            throws ApiException;

    /**
     * 删除
     *
     * @param list 角色标识请求列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    Boolean delete(@RequestBody @ApiParam("角色标识请求列表") List<RoleIdRequest> list) throws ApiException;

    /**
     * 获取菜单树
     *
     * @return 菜单树
     */
    @ApiOperation(value = "获取菜单树", notes = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "menu/tree", method = RequestMethod.POST)
    List<RoleMenuResponse> menuTree();

    /**
     * 获取用户树
     *
     * @return 菜单树
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取用户树", notes = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "user/tree", method = RequestMethod.POST)
    List<RoleUserTreeNodeResponse> userTree() throws ApiException;

    /**
     * 获取权限用户列表
     *
     * @param request 角色标识请求
     * @return 权限
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取权限用户列表", notes = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "user/list", method = RequestMethod.POST)
    List<RoleUserResponse> userList(@RequestBody @ApiParam("角色标识请求") RoleIdRequest request)
            throws ApiException;

    /**
     * 授权用户
     *
     * @param request 角色授权用户请求
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新权限用户列表", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("授权")
    @RequestMapping(value = "user/assign", method = RequestMethod.POST)
    Boolean assignUser(@RequestBody @ApiParam("角色授权用户请求") RoleAssignUserRequest request)
            throws ApiException;
}
