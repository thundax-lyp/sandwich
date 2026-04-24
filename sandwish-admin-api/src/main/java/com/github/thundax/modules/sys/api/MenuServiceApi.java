package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.query.MoveTreeNodeQueryParam;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.api.query.MenuQueryParam;
import com.github.thundax.modules.sys.api.vo.MenuVo;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author wdit
 */
@Api(tags = "02-03.系统-菜单")
@SysLogger(module = {"系统", "菜单"})
@RequestMapping(value = "/api/sys/menu")
public interface MenuServiceApi {

    /**
     * 获取对象
     *
     * @param menu 菜单
     * @return 菜单
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取对象", notes = "super")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    MenuVo get(@RequestBody @ApiParam("菜单") MenuVo menu) throws ApiException;


    /**
     * 获取列表
     *
     * @param queryParam 查询条件
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "super")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    List<MenuVo> list(@RequestBody @ApiParam("查询条件") MenuQueryParam queryParam) throws ApiException;


    /**
     * 添加
     *
     * @param menu 菜单
     * @return 菜单
     * @throws ApiException API异常
     */
    @ApiOperation(value = "添加", notes = "super")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    MenuVo add(@RequestBody @ApiParam(value = "菜单") MenuVo menu) throws ApiException;


    /**
     * 更新
     *
     * @param menu 菜单
     * @return 菜单
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新", notes = "super")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("修改")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    MenuVo update(@RequestBody @ApiParam("菜单") MenuVo menu) throws ApiException;


    /**
     * 显示/隐藏
     *
     * @param list 列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "显示/隐藏", notes = "super")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("显示")
    @RequestMapping(value = "display", method = RequestMethod.POST)
    Boolean updateDisplayFlag(@RequestBody @ApiParam("菜单列表") List<MenuVo> list) throws ApiException;


    /**
     * 删除
     *
     * @param list 列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除", notes = "super")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    Boolean delete(@RequestBody @ApiParam("菜单列表") List<MenuVo> list) throws ApiException;


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
    List<MenuVo> tree(@RequestBody @ApiParam("菜单列表") List<MenuVo> excludeList) throws ApiException;


    /**
     * 移动
     *
     * @param queryParam 移动参数
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "排序", notes = "super")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("排序")
    @RequestMapping(value = "move", method = RequestMethod.POST)
    Boolean move(@RequestBody @ApiParam("移动参数") MoveTreeNodeQueryParam queryParam) throws ApiException;

}
