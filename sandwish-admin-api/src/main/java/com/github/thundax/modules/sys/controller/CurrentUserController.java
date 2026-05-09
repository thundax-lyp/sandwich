package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.permission.PermissionAuthorities;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.query.PreAuthSessionQuery;
import com.github.thundax.modules.auth.service.query.PrincipalIdentityQuery;
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
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.CurrentUserService;
import com.github.thundax.modules.sys.service.command.ChangeCurrentUserInfoCommand;
import com.github.thundax.modules.sys.service.command.ChangeCurrentUserPasswordCommand;
import com.github.thundax.modules.sys.service.query.CurrentUserQuery;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "系统/当前用户")
@SysLogger(module = {"系统", "当前用户"})
@RequestMapping(value = "/api/sys/current-user")
@WrappedApiController
public class CurrentUserController {

    private static final String PRIVATE_KEY_ITEM = "privateKey";

    private final CurrentUserService currentUserService;
    private final PrincipalIdentityService principalIdentityService;
    private final PreAuthSessionService preAuthSessionService;

    public CurrentUserController(
            CurrentUserService currentUserService,
            PrincipalIdentityService principalIdentityService,
            PreAuthSessionService preAuthSessionService) {

        this.currentUserService = currentUserService;
        this.principalIdentityService = principalIdentityService;
        this.preAuthSessionService = preAuthSessionService;
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
    @PostMapping(value = "info")
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
    @PostMapping(value = "info/update")
    public PersonalInfoResponse updateInfo(@Valid @RequestBody PersonalInfoUpdateRequest request) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        currentUser = currentUserService.changeInfo(new ChangeCurrentUserInfoCommand(
                currentUser.getId(),
                currentUser.getDepartmentId(),
                request.getEmail(),
                request.getMobile(),
                currentUser.getTel(),
                request.getName(),
                currentUser.getRank(),
                currentUser.getPrivilege(),
                currentUser.getStatus(),
                currentUser.getPriority(),
                currentUser.getRemarks()));

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
    @PostMapping(value = "password/update")
    public Boolean updatePassword(@Valid @RequestBody PersonalPasswordUpdateRequest request) throws ApiException {

        // 解密密码（数据需要加密传输）
        String privateKey = getPrivateKey(request.getToken());
        String password = Sm2Helper.decrypt(request.getPassword(), privateKey);
        String oldPassword = Sm2Helper.decrypt(request.getOldPassword(), privateKey);
        request.setPassword(password);
        request.setOldPassword(oldPassword);

        User currentUser = UserAccessHolder.currentUser();

        currentUserService.changePassword(
                new ChangeCurrentUserPasswordCommand(currentUser.getId(), oldPassword, password));

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
    @PostMapping(value = "avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PersonalAvatarResponse uploadAvatar(@Valid PersonalAvatarUploadRequest request) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        try {
            AvatarUtils.saveAvatar(
                    UserIdCodec.toStringValue(currentUser.getId()),
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
    @PostMapping(value = "avatar/delete")
    public PersonalAvatarResponse deleteAvatar() {
        User currentUser = UserAccessHolder.currentUser();

        AvatarUtils.deleteAvatar(UserIdCodec.toStringValue(currentUser.getId()));

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
    @PostMapping(value = "menus")
    public List<PersonalMenuResponse> menus() {
        return currentUserService.listVisibleMenus(toQuery(UserAccessHolder.currentUser())).stream()
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
    @PostMapping(value = "perms")
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
        PrincipalIdentity identity = principalIdentityService.get(identityQuery(
                PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(user.getId())),
                PrincipalIdentityType.USER_ACCOUNT));
        return identity == null ? null : identity.getIdentityValue();
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }

    private CurrentUserQuery toQuery(User currentUser) {
        return new CurrentUserQuery(
                currentUser.getId(), currentUser.getPrivilege(), currentUser.getStatus(), currentUser.getRank());
    }

    private String getPrivateKey(String token) throws InvalidTokenException {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByToken(
                new PreAuthSessionQuery(null, PreAuthSessionToken.of(token), null, null));
        if (sessionId == null) {
            throw new InvalidTokenException();
        }
        String privateKey =
                preAuthSessionService.getValue(new PreAuthSessionQuery(sessionId, null, null, PRIVATE_KEY_ITEM));
        if (StringUtils.isBlank(privateKey)) {
            throw new InvalidTokenException();
        }
        return privateKey;
    }
}
