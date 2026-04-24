package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.vo.UserVo;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.api.query.UserQueryParam;
import com.github.thundax.modules.sys.api.vo.OfficeVo;
import com.github.thundax.modules.sys.api.vo.RoleVo;
import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author wdit
 */
@Api(tags = "02-05.系统-用户")
@SysLogger(module = {"系统", "用户"})
@RequestMapping(value = "/api/sys/user")
public interface UserServiceApi {

    /**
     * 获取对象
     *
     * @param user 用户
     * @return 用户
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取对象", notes = "sys:user:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    UserVo get(@RequestBody @ApiParam("用户") UserVo user) throws ApiException;


    /**
     * 获取列表
     *
     * @param queryParam 用户查询参数
     * @return 用户列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "sys:user:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    List<UserVo> list(@RequestBody @ApiParam("用户查询参数") UserQueryParam queryParam) throws ApiException;


    /**
     * 获取分页列表
     *
     * @param queryParam 用户查询参数
     * @return 分页
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取分页列表", notes = "sys:user:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("分页")
    @RequestMapping(value = "page", method = RequestMethod.POST)
    PageVo<UserVo> page(@RequestBody @ApiParam("用户查询参数") UserQueryParam queryParam) throws ApiException;


    /**
     * 添加
     *
     * @param user 用户
     * @return 用户
     * @throws ApiException API异常
     */
    @ApiOperation(value = "添加", notes = "sys:user:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    UserVo add(@RequestBody @ApiParam("用户") UserVo user) throws ApiException;


    /**
     * 更新
     *
     * @param user 用户
     * @return 用户
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新", notes = "sys:user:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    UserVo update(@RequestBody @ApiParam("用户") UserVo user) throws ApiException;


    /**
     * 上传头像
     *
     * @param id     用户id
     * @param avatar 头像文件流
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "上传头像", notes = "sys:user:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataTypeClass = String.class),
    })
    @SysLogger("上传头像")
    @RequestMapping(value = "avatar/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Boolean uploadAvatar(@RequestParam(value = "id") String id,
                         @ApiParam(value = "头像文件", required = true) MultipartFile avatar) throws ApiException;


    /**
     * 删除头像
     *
     * @param user 用户
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除头像", notes = "sys:user:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("删除头像")
    @RequestMapping(value = "avatar/delete", method = RequestMethod.POST)
    Boolean deleteAvatar(@RequestBody @ApiParam("用户") UserVo user) throws ApiException;


    /**
     * 获取头像相对路径
     *
     * @param user 用户
     * @return 头像相对路径
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取头像相对路径", notes = "sys:user:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "avatar", method = RequestMethod.POST)
    String avatar(@RequestBody @ApiParam("用户") UserVo user) throws ApiException;


    /**
     * 启用/禁用
     *
     * @param list 用户列表
     * @return 影响记录数
     * @throws ApiException API异常
     */
    @ApiOperation(value = "启用/禁用", notes = "sys:user:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("启用")
    @RequestMapping(value = "enable", method = RequestMethod.POST)
    Boolean updateEnableFlag(@RequestBody @ApiParam("用户列表") List<UserVo> list) throws ApiException;


    /**
     * 删除
     *
     * @param list 用户列表
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除", notes = "sys:user:edit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    Boolean delete(@RequestBody @ApiParam("用户列表") List<UserVo> list) throws ApiException;


    /**
     * 检查 [loginName]是否合法
     *
     * @param user 用户
     * @return 合法:true, 不合法:false
     */
    @ApiOperation(value = "检查 [loginName]是否存在", notes = "sys:user:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "check", method = RequestMethod.POST)
    Boolean check(@RequestBody @ApiParam("用户") UserVo user);


    /**
     * 检查 [ssoLoginName]是否合法
     *
     * @param user 用户
     * @return 合法:true, 不合法:false
     */
    @ApiOperation(value = "检查 [ssoLoginName]是否存在", notes = "sys:user:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "check-sso-loginName", method = RequestMethod.POST)
    Boolean checkSsoLoginName(@RequestBody @ApiParam("用户") UserVo user);


    /**
     * 获取部门树
     *
     * @return 部门列表
     */
    @ApiOperation(value = "获取部门树", notes = "sys:user:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "office/tree", method = RequestMethod.POST)
    List<OfficeVo> officeTree();


    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    @ApiOperation(value = "获取权限列表", notes = "sys:user:view")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "role/list", method = RequestMethod.POST)
    List<RoleVo> roleList();


    /**
     * 用户头像
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IO异常
     */
    @ApiOperation(value = "用户头像", notes = "user")
    @GetMapping(value = "avatar")
    void avatarImage(HttpServletRequest request, HttpServletResponse response) throws IOException;

}
