package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.security.permission.PermissionAuthorities;
import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.service.KeypairService;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.PersonalInterfaceAssembler;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.request.PersonalAvatarDeleteRequest;
import com.github.thundax.modules.sys.request.PersonalAvatarUploadRequest;
import com.github.thundax.modules.sys.request.PersonalInfoUpdateRequest;
import com.github.thundax.modules.sys.request.PersonalPasswordUpdateRequest;
import com.github.thundax.modules.sys.response.PersonalAvatarResponse;
import com.github.thundax.modules.sys.response.PersonalInfoResponse;
import com.github.thundax.modules.sys.response.PersonalMenuResponse;
import com.github.thundax.modules.sys.response.PersonalPermsResponse;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.utils.SysApiUtils;
import com.github.thundax.modules.utils.AvatarUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "02-01.系统/个人")
@SysLogger(module = {"系统", "个人"})
@RequestMapping(value = "/api/sys/personal")
@RestController
public class PersonalApiController extends BaseApiController {

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final PasswordService passwordService;
    private final KeypairService keypairService;

    public PersonalApiController(
            UserService userService,
            RoleService roleService,
            MenuService menuService,
            PasswordService passwordService,
            KeypairService keypairService) {

        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
        this.passwordService = passwordService;
        this.keypairService = keypairService;
    }

    @ApiOperation(value = "当前用户信息", notes = "user")
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

        return PersonalInterfaceAssembler.toInfoResponse(currentUser);
    }

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
    public PersonalInfoResponse updateInfo(@Valid @RequestBody PersonalInfoUpdateRequest request) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        PersonalInterfaceAssembler.toEntity(currentUser, request);
        userService.update(currentUser);

        return PersonalInterfaceAssembler.toInfoResponse(currentUser);
    }

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
    public Boolean updatePassword(@Valid @RequestBody PersonalPasswordUpdateRequest request) throws ApiException {

        // 解密密码（数据需要加密传输）
        String privateKey = keypairService.getPrivateKey(request.getToken());
        String password = Sm2.decrypt(request.getPassword(), privateKey);
        String oldPassword = Sm2.decrypt(request.getOldPassword(), privateKey);
        request.setPassword(password);
        request.setOldPassword(oldPassword);
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
    public PersonalAvatarResponse uploadAvatar(@Valid PersonalAvatarUploadRequest request) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        try {
            AvatarUtils.saveAvatar(
                    EntityIdCodec.toValue(currentUser.getId()),
                    request.getAvatar().getInputStream());
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        }

        return PersonalInterfaceAssembler.toAvatarResponse(currentUser);
    }

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
    public PersonalAvatarResponse deleteAvatar(@RequestBody(required = false) PersonalAvatarDeleteRequest request) {
        User currentUser = UserAccessHolder.currentUser();

        AvatarUtils.deleteAvatar(EntityIdCodec.toValue(currentUser.getId()));

        return PersonalInterfaceAssembler.toAvatarResponse(currentUser);
    }

    @ApiOperation(value = "菜单列表", notes = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "menus", method = RequestMethod.POST)
    public List<PersonalMenuResponse> menus() {
        // 获取可见菜单
        List<Menu> allMenuList = findMenuList(UserAccessHolder.currentUser());

        Menu rootMenu = new Menu();
        List<Menu> menuList = Lists.newArrayList(rootMenu);
        // 构建parent-child关系，并递归下去
        for (int idx = 0; idx < menuList.size(); idx++) {
            Menu parent = menuList.get(idx);

            List<Menu> childList = allMenuList == null
                    ? new ArrayList<>()
                    : allMenuList.stream()
                            .filter(item -> item.isDisplay() && Objects.equals(item.getParentId(), parent.getId()))
                            .collect(Collectors.toList());

            menuList.addAll(idx + 1, childList);
        }
        // 移除根节点
        menuList.remove(0);

        return menuList.stream()
                .map(menu -> PersonalInterfaceAssembler.toMenuResponse(menu))
                .collect(Collectors.toList());
    }

    private List<Menu> findMenuList(User user) {
        List<String> menuIdList;

        if (user.isSuper()) {
            menuIdList = menuService.list(new Menu()).stream()
                    .map(menu -> EntityIdCodec.toValue(menu.getId()))
                    .collect(Collectors.toList());
        } else {
            List<Role> roleList = userService.listUserRoles(user);
            boolean isAdmin = user.isAdmin() || roleList.stream().anyMatch(Role::isAdmin);

            if (isAdmin) {
                menuIdList = menuService.list(user.getRanks()).stream()
                        .map(menu -> EntityIdCodec.toValue(menu.getId()))
                        .collect(Collectors.toList());
            } else {
                Set<String> menuIds = Sets.newHashSet();
                for (Role role : roleList) {
                    menuIds.addAll(roleService.listRoleMenus(role).stream()
                            .map(menu -> EntityIdCodec.toValue(menu.getId()))
                            .collect(Collectors.toList()));
                }
                menuIds.removeIf(menuId -> {
                    Menu menu = menuService.getById(EntityIdCodec.toDomain(menuId));
                    return menu == null || menu.getRanks() > user.getRanks();
                });
                menuIdList = new ArrayList<>(menuIds);
            }
        }

        List<Menu> menuList = menuService.batchGetByIds(menuIdList);
        menuList.sort(Menu::compareTo);
        return menuList;
    }

    @ApiOperation(value = "权限列表", notes = "user")
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
}
