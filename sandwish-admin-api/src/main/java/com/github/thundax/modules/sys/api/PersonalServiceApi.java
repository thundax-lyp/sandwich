package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.request.PersonalAvatarDeleteRequest;
import com.github.thundax.modules.sys.request.PersonalAvatarUploadRequest;
import com.github.thundax.modules.sys.request.PersonalInfoUpdateRequest;
import com.github.thundax.modules.sys.request.PersonalPasswordUpdateRequest;
import com.github.thundax.modules.sys.response.PersonalAvatarResponse;
import com.github.thundax.modules.sys.response.PersonalInfoResponse;
import com.github.thundax.modules.sys.response.PersonalMenuResponse;
import com.github.thundax.modules.sys.response.PersonalPermsResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "info", method = RequestMethod.POST)
    PersonalInfoResponse info() throws ApiException;

    /**
     * 更新用户信息，包括：name, email, mobile
     *
     * @param request 个人资料更新请求
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新用户信息，包括：name, email, mobile", notes = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    PersonalInfoResponse updateInfo(@RequestBody @ApiParam("个人资料更新请求") PersonalInfoUpdateRequest request)
            throws ApiException;

    /**
     * 更新用户密码
     *
     * @param request 个人密码更新请求
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "更新用户密码", notes = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("更新密码")
    @RequestMapping(value = "password", method = RequestMethod.POST)
    Boolean updatePassword(@RequestBody @ApiParam("个人密码更新请求") PersonalPasswordUpdateRequest request)
            throws ApiException;

    /**
     * 上传头像
     *
     * @param request 个人头像上传请求
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "上传头像", notes = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("上传头像")
    @RequestMapping(
            value = "avatar/upload",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    PersonalAvatarResponse uploadAvatar(@ModelAttribute @ApiParam("个人头像上传请求") PersonalAvatarUploadRequest request)
            throws ApiException;

    /**
     * 删除头像
     *
     * @param request 个人头像删除请求
     * @return 成功:true, 失败:false
     * @throws ApiException API异常
     */
    @ApiOperation(value = "删除头像", notes = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("删除头像")
    @RequestMapping(value = "avatar/delete", method = RequestMethod.POST)
    PersonalAvatarResponse deleteAvatar(
            @RequestBody(required = false) @ApiParam("个人头像删除请求") PersonalAvatarDeleteRequest request)
            throws ApiException;

    /**
     * 菜单列表
     *
     * @return 菜单列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "菜单列表", notes = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "menus", method = RequestMethod.POST)
    List<PersonalMenuResponse> menus() throws ApiException;

    /**
     * 权限列表
     *
     * @return 权限列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "权限列表", notes = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "perms", method = RequestMethod.POST)
    PersonalPermsResponse perms() throws ApiException;
}
