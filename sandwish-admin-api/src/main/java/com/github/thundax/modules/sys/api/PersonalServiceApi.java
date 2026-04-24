package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.UserVo;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.api.query.UpdatePasswordQueryParam;
import com.github.thundax.modules.sys.api.vo.MenuVo;
import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author wdit
 */
@Api(tags = "02-01.系统/个人")
@SysLogger(module = {"系统", "个人"})
@RequestMapping(value = "/api/sys/personal")
public interface PersonalServiceApi {

    /**
     * 当前用户信息
     *
     * @return 用户
     * @throws ApiException API异常
     */
    @ApiOperation(value = "当前用户信息", notes = "user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "info", method = RequestMethod.POST)
    UserVo info() throws ApiException;


    /**
     * 更新用户信息，包括：name, email, mobile
     *
     * @param user 用户
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新用户信息，包括：name, email, mobile", notes = "user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    UserVo updateInfo(@RequestBody @ApiParam("用户信息") UserVo user) throws ApiException;


    /**
     * 更新用户密码
     *
     * @param queryParam 更新密码参数
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新用户密码", notes = "user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("更新密码")
    @RequestMapping(value = "password", method = RequestMethod.POST)
    Boolean updatePassword(@RequestBody @ApiParam("更新密码参数") UpdatePasswordQueryParam queryParam) throws ApiException;


    /**
     * 上传头像
     *
     * @param avatar 头像文件
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "上传头像", notes = "user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("上传头像")
    @RequestMapping(value = "avatar/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UserVo uploadAvatar(@ApiParam(value = "头像文件", required = true) MultipartFile avatar) throws ApiException;


    /**
     * 删除头像
     * UserVo
     *
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除头像", notes = "user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @SysLogger("删除头像")
    @RequestMapping(value = "avatar/delete", method = RequestMethod.POST)
    UserVo deleteAvatar() throws ApiException;


    /**
     * 菜单列表
     *
     * @return 菜单列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "菜单列表", notes = "user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "menus", method = RequestMethod.POST)
    List<MenuVo> menus() throws ApiException;


    /**
     * 权限列表
     *
     * @return 权限列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "权限列表", notes = "user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "perms", method = RequestMethod.POST)
    Set<String> perms() throws ApiException;

}
