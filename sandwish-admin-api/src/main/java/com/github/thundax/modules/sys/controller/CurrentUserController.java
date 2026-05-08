package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.permission.PermissionAuthorities;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.AdminAuthService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.PersonalInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.PersonalAvatarUploadRequest;
import com.github.thundax.modules.sys.controller.request.PersonalInfoUpdateRequest;
import com.github.thundax.modules.sys.controller.request.PersonalPasswordUpdateRequest;
import com.github.thundax.modules.sys.controller.response.PersonalAvatarResponse;
import com.github.thundax.modules.sys.controller.response.PersonalInfoResponse;
import com.github.thundax.modules.sys.controller.response.PersonalMenuResponse;
import com.github.thundax.modules.sys.controller.response.PersonalPermsResponse;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.CurrentUserService;
import com.github.thundax.modules.utils.AvatarUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "系统/当前用户")
@SysLogger(module = {"系统", "当前用户"})
@RequestMapping(value = "/api/sys/current-user")
@WrappedApiController
public class CurrentUserController {

    private final CurrentUserService currentUserService;
    private final PrincipalIdentityService principalIdentityService;
    private final AdminAuthService authService;

    public CurrentUserController(
            CurrentUserService currentUserService,
            PrincipalIdentityService principalIdentityService,
            AdminAuthService authService) {

        this.currentUserService = currentUserService;
        this.principalIdentityService = principalIdentityService;
        this.authService = authService;
    }

    @ApiOperation(value = "当前用户信息", notes = "读取当前登录后台用户的基础资料和登录名")
    @HasPermission("user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "info", method = RequestMethod.POST)
    public PersonalInfoResponse info() throws ApiException {
        User currentUser = UserAccessHolder.currentUser();
        if (currentUser.getId() == null || !currentUser.isEnable()) {
            throw new InvalidTokenException();
        }

        return PersonalInterfaceAssembler.toInfoResponse(currentUser, getAccountLoginName(currentUser));
    }

    @ApiOperation(value = "更新当前用户信息", notes = "更新当前登录后台用户的姓名、邮箱和手机号")
    @HasPermission("user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "info/update", method = RequestMethod.POST)
    public PersonalInfoResponse updateInfo(@Valid @RequestBody PersonalInfoUpdateRequest request) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        currentUser =
                currentUserService.updateInfo(currentUser, request.getName(), request.getEmail(), request.getMobile());

        return PersonalInterfaceAssembler.toInfoResponse(currentUser, getAccountLoginName(currentUser));
    }

    @ApiOperation(value = "更新当前用户密码", notes = "校验当前登录后台用户旧密码后更新密码凭据")
    @HasPermission("user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("更新密码")
    @RequestMapping(value = "password/update", method = RequestMethod.POST)
    public Boolean updatePassword(@Valid @RequestBody PersonalPasswordUpdateRequest request) throws ApiException {

        // 解密密码（数据需要加密传输）
        String privateKey = authService.getPrivateKey(request.getToken());
        String password = Sm2Helper.decrypt(request.getPassword(), privateKey);
        String oldPassword = Sm2Helper.decrypt(request.getOldPassword(), privateKey);
        request.setPassword(password);
        request.setOldPassword(oldPassword);

        User currentUser = UserAccessHolder.currentUser();

        currentUserService.updatePassword(currentUser, oldPassword, password);

        return true;
    }

    @ApiOperation(value = "上传当前用户头像", notes = "保存当前登录后台用户头像文件并返回头像访问信息")
    @HasPermission("user")
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
    public PersonalAvatarResponse uploadAvatar(@Valid PersonalAvatarUploadRequest request) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        try {
            AvatarUtils.saveAvatar(
                    EntityIdCodec.toStringValue(currentUser.getId()),
                    request.getAvatar().getInputStream());
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        }

        return PersonalInterfaceAssembler.toAvatarResponse(currentUser);
    }

    @ApiOperation(value = "删除当前用户头像", notes = "删除当前登录后台用户头像文件并返回头像访问信息")
    @HasPermission("user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("删除头像")
    @RequestMapping(value = "avatar/delete", method = RequestMethod.POST)
    public PersonalAvatarResponse deleteAvatar() {
        User currentUser = UserAccessHolder.currentUser();

        AvatarUtils.deleteAvatar(EntityIdCodec.toStringValue(currentUser.getId()));

        return PersonalInterfaceAssembler.toAvatarResponse(currentUser);
    }

    @ApiOperation(value = "当前用户菜单列表", notes = "按当前登录后台用户角色和访问等级返回可见菜单树列表")
    @HasPermission("user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "menus", method = RequestMethod.POST)
    public List<PersonalMenuResponse> menus() {
        return currentUserService.listVisibleMenus(UserAccessHolder.currentUser()).stream()
                .map(PersonalInterfaceAssembler::toMenuResponse)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "当前用户权限列表", notes = "返回当前登录后台用户认证上下文中的权限编码集合")
    @HasPermission("user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "perms", method = RequestMethod.POST)
    public PersonalPermsResponse perms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return PersonalInterfaceAssembler.toPermsResponse(new HashSet<>());
        }

        return PersonalInterfaceAssembler.toPermsResponse(
                PermissionAuthorities.toPermissions(authentication.getAuthorities()));
    }

    private String getAccountLoginName(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        PrincipalIdentity identity = principalIdentityService.getByPrincipalKeyAndType(
                PrincipalKey.of(PrincipalType.USER, user.getId()), PrincipalIdentityType.USER_ACCOUNT);
        return identity == null ? null : identity.getIdentityValue();
    }
}
