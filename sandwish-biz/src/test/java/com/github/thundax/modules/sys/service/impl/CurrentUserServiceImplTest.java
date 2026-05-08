package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.utils.PasswordHelper;
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
import org.mockito.ArgumentCaptor;

public class CurrentUserServiceImplTest {

    @Test
    public void shouldReturnVisibleMenusInTreeOrderWhenParentIdMatchesMenuIdValue() {
        UserService userService = mock(UserService.class);
        MenuService menuService = mock(MenuService.class);
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                userService,
                mock(RoleService.class),
                menuService,
                mock(UserCredentialService.class),
                mock(UserIdentityService.class));
        List<Menu> menus = Arrays.asList(menu(5001L, null, "系统管理"), menu(5002L, 5001L, "用户管理"));

        when(menuService.list(any(MenuQuery.class))).thenReturn(menus);
        when(menuService.listByIds(anyList())).thenReturn(menus);

        List<Menu> responses = service.listVisibleMenus(superUser());

        assertEquals(2, responses.size());
        assertEquals(Long.valueOf(5001L), EntityIdCodec.toValue(responses.get(0).getId()));
        assertEquals(Long.valueOf(5002L), EntityIdCodec.toValue(responses.get(1).getId()));
        assertEquals(Long.valueOf(5001L), EntityIdCodec.toValue(responses.get(1).getParentId()));
    }

    @Test
    public void shouldOnlyReturnVisibleMenusReachableFromRoot() {
        UserService userService = mock(UserService.class);
        MenuService menuService = mock(MenuService.class);
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                userService,
                mock(RoleService.class),
                menuService,
                mock(UserCredentialService.class),
                mock(UserIdentityService.class));
        List<Menu> menus = Arrays.asList(
                menu(5001L, null, "系统管理"),
                menu(5002L, 5001L, "用户管理"),
                menu(5003L, null, "隐藏菜单", MenuVisibility.HIDDEN),
                menu(5004L, 5003L, "隐藏子菜单"),
                menu(5005L, 5099L, "散落菜单"));

        when(menuService.list(any(MenuQuery.class))).thenReturn(menus);
        when(menuService.listByIds(anyList())).thenReturn(menus);

        List<Menu> responses = service.listVisibleMenus(superUser());

        assertEquals(2, responses.size());
        assertEquals(Long.valueOf(5001L), EntityIdCodec.toValue(responses.get(0).getId()));
        assertEquals(Long.valueOf(5002L), EntityIdCodec.toValue(responses.get(1).getId()));
    }

    @Test
    public void shouldReturnVisibleMenuDescendantsReachableFromRoot() {
        UserService userService = mock(UserService.class);
        MenuService menuService = mock(MenuService.class);
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                userService,
                mock(RoleService.class),
                menuService,
                mock(UserCredentialService.class),
                mock(UserIdentityService.class));
        List<Menu> menus = Arrays.asList(
                menu(5010L, null, "A-root"), menu(5011L, 5010L, "B-child"), menu(5012L, 5011L, "C-grandchild"));

        when(menuService.list(any(MenuQuery.class))).thenReturn(menus);

        List<Menu> responses = service.listVisibleMenus(superUser());

        assertEquals(3, responses.size());
        assertEquals(Long.valueOf(5010L), EntityIdCodec.toValue(responses.get(0).getId()));
        assertEquals(Long.valueOf(5011L), EntityIdCodec.toValue(responses.get(1).getId()));
        assertEquals(Long.valueOf(5012L), EntityIdCodec.toValue(responses.get(2).getId()));
    }

    @Test
    public void shouldUpdateCurrentUserInfo() {
        UserService userService = mock(UserService.class);
        UserIdentityService userIdentityService = mock(UserIdentityService.class);
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                userService,
                mock(RoleService.class),
                mock(MenuService.class),
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
        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                userService,
                mock(RoleService.class),
                mock(MenuService.class),
                userCredentialService,
                mock(UserIdentityService.class));
        User currentUser = superUser();
        UserCredential credential = new UserCredential();
        credential.setCredentialValue(PasswordHelper.encrypt("OldPass1$"));

        when(userCredentialService.getPasswordCredential(currentUser.getId())).thenReturn(credential);

        service.updatePassword(currentUser, "OldPass1$", "NewPass1$");

        ArgumentCaptor<String> encryptedPasswordCaptor = ArgumentCaptor.forClass(String.class);
        verify(userCredentialService).upsertPassword(eq(currentUser), encryptedPasswordCaptor.capture());
        assertEquals(true, PasswordHelper.validate("NewPass1$", encryptedPasswordCaptor.getValue()));
    }

    private User superUser() {
        User user = new User();
        user.setId(EntityId.of(1001L));
        user.setPrivilege(UserPrivilege.SUPER);
        return user;
    }

    private Menu menu(Long id, Long parentId, String name) {
        return menu(id, parentId, name, MenuVisibility.VISIBLE);
    }

    private Menu menu(Long id, Long parentId, String name, MenuVisibility visibility) {
        Menu menu = new Menu();
        menu.setId(EntityId.of(id));
        menu.setParentId(EntityId.ofNullable(parentId));
        menu.setName(name);
        menu.setVisibility(visibility);
        return menu;
    }
}
