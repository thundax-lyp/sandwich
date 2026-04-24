package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.UserVo;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.api.query.AssignUserQueryParam;
import com.github.thundax.modules.sys.api.query.RoleQueryParam;
import com.github.thundax.modules.sys.api.vo.MenuVo;
import com.github.thundax.modules.sys.api.vo.RoleVo;
import com.github.thundax.modules.sys.api.vo.UserTreeNodeVo;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author wdit
 */
@Api(tags = "02-04.系统-权限")
@SysLogger(module = {"系统", "权限"})
@RequestMapping(value = "/api/sys/role")
public interface RoleServiceApi {

    /**
     * 获取对象
     *
     * @param role 权限
     * @return 权限
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取对象", notes = "sys:role:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),

    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    RoleVo get(@RequestBody @ApiParam("权限") RoleVo role) throws ApiException;


    /**
     * 获取列表
     *
     * @param queryParam 权限查询参数
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "sys:role:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    List<RoleVo> list(@RequestBody @ApiParam("查询参数") RoleQueryParam queryParam) throws ApiException;


    /**
     * 添加
     *
     * @param role 权限
     * @return 权限
     * @throws ApiException API异常
     */
    @ApiOperation(value = "添加", notes = "sys:role:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    RoleVo add(@RequestBody @ApiParam(value = "权限") RoleVo role) throws ApiException;


    /**
     * 更新
     *
     * @param role 权限
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新", notes = "sys:role:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    RoleVo update(@RequestBody @ApiParam("权限") RoleVo role) throws ApiException;


    /**
     * 启用/禁用
     *
     * @param list 权限列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "启用/禁用", notes = "sys:role:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("启用")
    @RequestMapping(value = "enable", method = RequestMethod.POST)
    Boolean updateEnableFlag(@RequestBody @ApiParam("权限列表") List<RoleVo> list) throws ApiException;


    /**
     * 排序
     *
     * @param list 权限列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "排序", notes = "sys:role:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("排序")
    @RequestMapping(value = "priority", method = RequestMethod.POST)
    Boolean updatePriority(@RequestBody @ApiParam("权限列表") List<RoleVo> list) throws ApiException;


    /**
     * 删除
     *
     * @param list 权限列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除", notes = "sys:role:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    Boolean delete(@RequestBody @ApiParam("权限列表") List<RoleVo> list) throws ApiException;


    /**
     * 获取菜单树
     *
     * @return 菜单树
     */
    @ApiOperation(value = "获取菜单树", notes = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),

    })
    @RequestMapping(value = "menu/tree", method = RequestMethod.POST)
    List<MenuVo> menuTree();


    /**
     * 获取用户树
     *
     * @return 菜单树
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取用户树", notes = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "user/tree", method = RequestMethod.POST)
    List<UserTreeNodeVo> userTree() throws ApiException;


    /**
     * 获取权限用户列表
     *
     * @param role 权限
     * @return 权限
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取权限用户列表", notes = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "user/list", method = RequestMethod.POST)
    List<UserVo> userList(@RequestBody @ApiParam("权限") RoleVo role) throws ApiException;


    /**
     * 授权用户
     *
     * @param queryParam 授权参数
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新权限用户列表", notes = "sys:role:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),

    })
    @SysLogger("授权")
    @RequestMapping(value = "user/assign", method = RequestMethod.POST)
    Boolean assignUser(@RequestBody @ApiParam("授权参数") AssignUserQueryParam queryParam) throws ApiException;

}
