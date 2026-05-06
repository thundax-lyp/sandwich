package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.permission.PermissionAuthorities;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.modules.assist.service.KeypairService;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.PersonalInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.PersonalAvatarDeleteRequest;
import com.github.thundax.modules.sys.controller.request.PersonalAvatarUploadRequest;
import com.github.thundax.modules.sys.controller.request.PersonalInfoUpdateRequest;
import com.github.thundax.modules.sys.controller.request.PersonalPasswordUpdateRequest;
import com.github.thundax.modules.sys.controller.response.PersonalAvatarResponse;
import com.github.thundax.modules.sys.controller.response.PersonalInfoResponse;
import com.github.thundax.modules.sys.controller.response.PersonalMenuResponse;
import com.github.thundax.modules.sys.controller.response.PersonalPermsResponse;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;
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

@Api(tags = "系统/当前用户")
@SysLogger(module = {"系统", "当前用户"})
@RequestMapping(value = "/api/sys/current-user")
@WrappedApiController
public class PersonalController {

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final PasswordService passwordService;
    private final KeypairService keypairService;

    public PersonalController(
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

        return PersonalInterfaceAssembler.toInfoResponse(
                currentUser, userService.getAccountLoginName(currentUser.getId()));
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

        PersonalInterfaceAssembler.toEntity(currentUser, request);
        userService.update(currentUser, userService.getAccountLoginName(currentUser.getId()));

        return PersonalInterfaceAssembler.toInfoResponse(
                currentUser, userService.getAccountLoginName(currentUser.getId()));
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
        String privateKey = keypairService.getPrivateKey(request.getToken());
        String password = Sm2Helper.decrypt(request.getPassword(), privateKey);
        String oldPassword = Sm2Helper.decrypt(request.getOldPassword(), privateKey);
        request.setPassword(password);
        request.setOldPassword(oldPassword);
        if (StringUtils.isBlank(password)) {
            throw new InvalidParameterException("password");
        } else if (!password.matches(SysApiUtils.PASSWORD_VALIDATE_PATTERN)) {
            throw new ApiException(SysApiUtils.PASSWORD_VALIDATE_MESSAGE);
        }

        User currentUser = UserAccessHolder.currentUser();

        UserCredential credential = userService.getPasswordCredential(currentUser.getId());
        if (credential == null || !passwordService.validate(oldPassword, credential.getCredentialValue())) {
            throw new InvalidPasswordException();
        }

        userService.updatePassword(
                currentUser.getId(), passwordService.encrypt(password), EntityIdCodec.toValue(currentUser.getId()));

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
                    EntityIdCodec.toValue(currentUser.getId()),
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
    public PersonalAvatarResponse deleteAvatar(
            @Valid @RequestBody(required = false) PersonalAvatarDeleteRequest request) {
        User currentUser = UserAccessHolder.currentUser();

        AvatarUtils.deleteAvatar(EntityIdCodec.toValue(currentUser.getId()));

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
                            .filter(item -> item.isDisplay() && StringUtils.equals(item.getParentId(), menuId(parent)))
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
                menuIdList = menuService.list(user.getRank()).stream()
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
                    return menu == null || !user.getRank().canAccess(menu.getRank());
                });
                menuIdList = new ArrayList<>(menuIds);
            }
        }

        List<Menu> menuList = menuService.listByIds(EntityIdCodec.toDomains(menuIdList));
        menuList.sort(Menu::compareTo);
        return menuList;
    }

    private String menuId(Menu menu) {
        return menu == null ? null : EntityIdCodec.toValue(menu.getId());
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
}
