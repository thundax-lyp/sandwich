package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserCredentialService;
import com.github.thundax.modules.sys.service.UserIdentityService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class CurrentUserServiceImplTest {

    @Test
    public void shouldReturnVisibleMenusInTreeOrderWhenParentIdMatchesMenuIdValue() {
        UserService userService = mock(UserService.class);
        MenuService menuService = mock(MenuService.class);
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                userService,
                mock(RoleService.class),
                menuService,
                mock(PasswordService.class),
                mock(UserCredentialService.class),
                mock(UserIdentityService.class));
        List<Menu> menus = Arrays.asList(menu("menu-system", null, "系统管理"), menu("menu-user", "menu-system", "用户管理"));

        when(menuService.list(any(MenuQuery.class))).thenReturn(menus);
        when(menuService.listByIds(anyList())).thenReturn(menus);

        List<Menu> responses = service.listVisibleMenus(superUser());

        assertEquals(2, responses.size());
        assertEquals("menu-system", EntityIdCodec.toValue(responses.get(0).getId()));
        assertEquals("menu-user", EntityIdCodec.toValue(responses.get(1).getId()));
        assertEquals("menu-system", EntityIdCodec.toValue(responses.get(1).getParentId()));
    }

    @Test
    public void shouldOnlyReturnVisibleMenusReachableFromRoot() {
        UserService userService = mock(UserService.class);
        MenuService menuService = mock(MenuService.class);
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                userService,
                mock(RoleService.class),
                menuService,
                mock(PasswordService.class),
                mock(UserCredentialService.class),
                mock(UserIdentityService.class));
        List<Menu> menus = Arrays.asList(
                menu("menu-system", null, "系统管理"),
                menu("menu-user", "menu-system", "用户管理"),
                menu("menu-hidden", null, "隐藏菜单", MenuVisibility.HIDDEN),
                menu("menu-hidden-child", "menu-hidden", "隐藏子菜单"),
                menu("menu-orphan", "menu-missing", "散落菜单"));

        when(menuService.list(any(MenuQuery.class))).thenReturn(menus);
        when(menuService.listByIds(anyList())).thenReturn(menus);

        List<Menu> responses = service.listVisibleMenus(superUser());

        assertEquals(2, responses.size());
        assertEquals("menu-system", EntityIdCodec.toValue(responses.get(0).getId()));
        assertEquals("menu-user", EntityIdCodec.toValue(responses.get(1).getId()));
    }

    @Test
    public void shouldReturnVisibleMenuDescendantsReachableFromRoot() {
        UserService userService = mock(UserService.class);
        MenuService menuService = mock(MenuService.class);
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                userService,
                mock(RoleService.class),
                menuService,
                mock(PasswordService.class),
                mock(UserCredentialService.class),
                mock(UserIdentityService.class));
        List<Menu> menus = Arrays.asList(
                menu("menu-root", null, "A-root"),
                menu("menu-child", "menu-root", "B-child"),
                menu("menu-grandchild", "menu-child", "C-grandchild"));

        when(menuService.list(any(MenuQuery.class))).thenReturn(menus);

        List<Menu> responses = service.listVisibleMenus(superUser());

        assertEquals(3, responses.size());
        assertEquals("menu-root", EntityIdCodec.toValue(responses.get(0).getId()));
        assertEquals("menu-child", EntityIdCodec.toValue(responses.get(1).getId()));
        assertEquals("menu-grandchild", EntityIdCodec.toValue(responses.get(2).getId()));
    }

    @Test
    public void shouldUpdateCurrentUserInfo() {
        UserService userService = mock(UserService.class);
        UserIdentityService userIdentityService = mock(UserIdentityService.class);
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                userService,
                mock(RoleService.class),
                mock(MenuService.class),
                mock(PasswordService.class),
                mock(UserCredentialService.class),
                userIdentityService);
        User currentUser = superUser();

        when(userIdentityService.getAccountLoginName(currentUser.getId())).thenReturn("tester");

        User updated = service.updateInfo(currentUser, "New Name", "new@example.com", "13800138000");

        assertEquals("New Name", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("13800138000", updated.getMobile());
        verify(userService).update(currentUser, "tester", null);
    }

    @Test
    public void shouldValidateOldPasswordAndUpdatePasswordCredential() throws Exception {
        UserService userService = mock(UserService.class);
        UserCredentialService userCredentialService = mock(UserCredentialService.class);
        PasswordService passwordService = mock(PasswordService.class);
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                userService,
                mock(RoleService.class),
                mock(MenuService.class),
                passwordService,
                userCredentialService,
                mock(UserIdentityService.class));
        User currentUser = superUser();
        UserCredential credential = new UserCredential();
        credential.setCredentialValue("encrypted-old");

        when(userCredentialService.getPasswordCredential(currentUser.getId())).thenReturn(credential);
        when(passwordService.validate("OldPass1$", "encrypted-old")).thenReturn(true);
        when(passwordService.encrypt("NewPass1$")).thenReturn("encrypted-new");

        service.updatePassword(currentUser, "OldPass1$", "NewPass1$");

        verify(userCredentialService).upsertPassword(currentUser, "encrypted-new");
    }

    private User superUser() {
        User user = new User();
        user.setId(EntityId.of("user-1"));
        user.setPrivilege(UserPrivilege.SUPER);
        return user;
    }

    private Menu menu(String id, String parentId, String name) {
        return menu(id, parentId, name, MenuVisibility.VISIBLE);
    }

    private Menu menu(String id, String parentId, String name, MenuVisibility visibility) {
        Menu menu = new Menu();
        menu.setId(EntityId.of(id));
        menu.setParentId(parentId);
        menu.setName(name);
        menu.setVisibility(visibility);
        return menu;
    }
}
