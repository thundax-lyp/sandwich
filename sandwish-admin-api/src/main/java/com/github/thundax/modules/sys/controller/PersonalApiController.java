package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.security.permission.PermissionAuthorities;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.service.KeypairService;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.api.PersonalServiceApi;
import com.github.thundax.modules.sys.assembler.PersonalInterfaceAssembler;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.request.PersonalAvatarDeleteRequest;
import com.github.thundax.modules.sys.request.PersonalAvatarUploadRequest;
import com.github.thundax.modules.sys.request.PersonalInfoUpdateRequest;
import com.github.thundax.modules.sys.request.PersonalPasswordUpdateRequest;
import com.github.thundax.modules.sys.response.PersonalAvatarResponse;
import com.github.thundax.modules.sys.response.PersonalInfoResponse;
import com.github.thundax.modules.sys.response.PersonalMenuResponse;
import com.github.thundax.modules.sys.response.PersonalPermsResponse;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.utils.SysApiUtils;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import com.github.thundax.modules.utils.AvatarUtils;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import javax.validation.Validator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** @author thundax */
@RestController
public class PersonalApiController extends BaseApiController implements PersonalServiceApi {

    private final UserService userService;
    private final PasswordService passwordService;
    private final KeypairService keypairService;
    private final PersonalInterfaceAssembler personalInterfaceAssembler;

    public PersonalApiController(
            Validator validator,
            UserService userService,
            PasswordService passwordService,
            KeypairService keypairService,
            PersonalInterfaceAssembler personalInterfaceAssembler) {
        super(validator);

        this.userService = userService;
        this.passwordService = passwordService;
        this.keypairService = keypairService;
        this.personalInterfaceAssembler = personalInterfaceAssembler;
    }

    @Override
    public PersonalInfoResponse info() throws ApiException {
        User currentUser = UserAccessHolder.currentUser();
        if (currentUser.getId() == null || !currentUser.isEnable()) {
            throw new InvalidTokenException();
        }

        return personalInterfaceAssembler.toInfoResponse(currentUser);
    }

    @Override
    public PersonalInfoResponse updateInfo(@RequestBody PersonalInfoUpdateRequest request)
            throws ApiException {
        validate(request);

        User currentUser = UserAccessHolder.currentUser();

        personalInterfaceAssembler.toEntity(currentUser, request);
        userService.save(currentUser);

        return personalInterfaceAssembler.toInfoResponse(currentUser);
    }

    @Override
    public Boolean updatePassword(@RequestBody PersonalPasswordUpdateRequest request)
            throws ApiException {

        // 解密密码（数据需要加密传输）
        String privateKey = keypairService.getPrivateKey(request.getToken());
        String password = Sm2.decrypt(request.getPassword(), privateKey);
        String oldPassword = Sm2.decrypt(request.getOldPassword(), privateKey);
        request.setPassword(password);
        request.setOldPassword(oldPassword);
        validate(request);

        if (StringUtils.isBlank(password)) {
            throw new InvalidParameterException("password");
        } else if (!password.matches(SysApiUtils.PASSWORD_VALIDATE_PATTERN)) {
            throw new ApiException(SysApiUtils.PASSWORD_VALIDATE_MESSAGE);
        }

        User currentUser = UserAccessHolder.currentUser();

        if (!passwordService.validate(oldPassword, currentUser.getLoginPass())) {
            throw new InvalidPasswordException();
        }

        currentUser.setLoginPass(passwordService.encrypt(password));
        userService.updatePassword(currentUser);

        return true;
    }

    @Override
    public PersonalAvatarResponse uploadAvatar(PersonalAvatarUploadRequest request)
            throws ApiException {
        validate(request);
        User currentUser = UserAccessHolder.currentUser();

        try {
            AvatarUtils.saveAvatar(currentUser.getId(), request.getAvatar().getInputStream());
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        }

        return personalInterfaceAssembler.toAvatarResponse(currentUser);
    }

    @Override
    public PersonalAvatarResponse deleteAvatar(
            @RequestBody(required = false) PersonalAvatarDeleteRequest request) {
        User currentUser = UserAccessHolder.currentUser();

        AvatarUtils.deleteAvatar(currentUser.getId());

        return personalInterfaceAssembler.toAvatarResponse(currentUser);
    }

    @Override
    public List<PersonalMenuResponse> menus() {
        // 获取可见菜单
        List<Menu> allMenuList = UserServiceHolder.findMenuList(currentUser());

        Menu rootMenu = new Menu(null);
        List<Menu> menuList = Lists.newArrayList(rootMenu);
        // 构建parent-child关系，并递归下去
        for (int idx = 0; idx < menuList.size(); idx++) {
            Menu parent = menuList.get(idx);

            List<Menu> childList =
                    ListUtils.filter(
                            allMenuList,
                            item ->
                                    item.isDisplay()
                                            && Objects.equals(item.getParentId(), parent.getId()));

            menuList.addAll(idx + 1, childList);
        }
        // 移除根节点
        menuList.remove(0);

        return ListUtils.map(menuList, personalInterfaceAssembler::toMenuResponse);
    }

    @Override
    public PersonalPermsResponse perms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return personalInterfaceAssembler.toPermsResponse(new HashSet<>());
        }

        return personalInterfaceAssembler.toPermsResponse(
                PermissionAuthorities.toPermissions(authentication.getAuthorities()));
    }
}
