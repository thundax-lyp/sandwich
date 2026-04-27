package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.request.OfficeIdRequest;
import com.github.thundax.modules.sys.request.OfficeMoveRequest;
import com.github.thundax.modules.sys.request.OfficeQueryRequest;
import com.github.thundax.modules.sys.request.OfficeSaveRequest;
import com.github.thundax.modules.sys.response.OfficeResponse;
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
@Api(tags = "02-02.系统-组织机构")
@SysLogger(module = {"系统", "组织机构"})
@RequestMapping(value = "/api/sys/office")
public interface OfficeServiceApi {

    /**
     * 获取对象
     *
     * @param request 机构标识请求
     * @return 组织机构
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取对象", notes = "sys:office:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    OfficeResponse get(@RequestBody @ApiParam("机构标识请求") OfficeIdRequest request)
            throws ApiException;

    /**
     * 获取列表
     *
     * @param request 机构查询请求
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "sys:office:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    List<OfficeResponse> list(@RequestBody @ApiParam("机构查询请求") OfficeQueryRequest request)
            throws ApiException;

    /**
     * 添加
     *
     * @param request 机构保存请求
     * @return 组织机构
     * @throws ApiException API异常
     */
    @ApiOperation(value = "添加", notes = "sys:office:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    OfficeResponse add(@RequestBody @ApiParam(value = "机构保存请求") OfficeSaveRequest request)
            throws ApiException;

    /**
     * 更新
     *
     * @param request 机构保存请求
     * @return 组织机构
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新", notes = "sys:office:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    OfficeResponse update(@RequestBody @ApiParam("机构保存请求") OfficeSaveRequest request)
            throws ApiException;

    /**
     * 删除
     *
     * @param list 机构标识请求列表
     * @return 影响记录数
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除", notes = "sys:office:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    Boolean delete(@RequestBody @ApiParam("机构标识请求列表") List<OfficeIdRequest> list)
            throws ApiException;

    /**
     * 获取树形结构
     *
     * @param excludeList 排除机构标识请求列表
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
    List<OfficeResponse> tree(
            @RequestBody @ApiParam("排除机构标识请求列表") List<OfficeIdRequest> excludeList)
            throws ApiException;

    /**
     * 移动
     *
     * @param request 机构树节点移动请求
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "移动", notes = "sys:office:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("移动")
    @RequestMapping(value = "move", method = RequestMethod.POST)
    Boolean move(@RequestBody @ApiParam("机构树节点移动请求") OfficeMoveRequest request) throws ApiException;
}
