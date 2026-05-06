package com.github.thundax.modules.sys.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.assist.service.KeypairService;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.controller.request.PersonalAvatarDeleteRequest;
import com.github.thundax.modules.sys.controller.request.PersonalAvatarUploadRequest;
import com.github.thundax.modules.sys.controller.request.PersonalInfoUpdateRequest;
import com.github.thundax.modules.sys.controller.request.PersonalPasswordUpdateRequest;
import com.github.thundax.modules.sys.controller.response.PersonalMenuResponse;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import io.swagger.annotations.Api;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class PersonalControllerContractTest {

    @After
    public void tearDown() {
        UserAccessHolder.clear();
        SpringContextHolder.clearHolder();
    }

    @Test
    public void shouldExposeCurrentUserBusinessPaths() throws NoSuchMethodException {
        assertEquals(
                "系统/当前用户", PersonalController.class.getAnnotation(Api.class).tags()[0]);
        assertMapping(PersonalController.class.getAnnotation(RequestMapping.class), "/api/sys/current-user");
        assertMapping(PersonalController.class.getMethod("info").getAnnotation(RequestMapping.class), "info");
        assertMapping(
                PersonalController.class
                        .getMethod("updateInfo", PersonalInfoUpdateRequest.class)
                        .getAnnotation(RequestMapping.class),
                "info/update");
        assertMapping(
                PersonalController.class
                        .getMethod("updatePassword", PersonalPasswordUpdateRequest.class)
                        .getAnnotation(RequestMapping.class),
                "password/update");
        assertMapping(
                PersonalController.class
                        .getMethod("uploadAvatar", PersonalAvatarUploadRequest.class)
                        .getAnnotation(RequestMapping.class),
                "avatar/upload");
        assertMapping(
                PersonalController.class
                        .getMethod("deleteAvatar", PersonalAvatarDeleteRequest.class)
                        .getAnnotation(RequestMapping.class),
                "avatar/delete");
        assertMapping(PersonalController.class.getMethod("menus").getAnnotation(RequestMapping.class), "menus");
        assertMapping(PersonalController.class.getMethod("perms").getAnnotation(RequestMapping.class), "perms");
    }

    @Test
    public void shouldReturnChildMenusWhenParentIdMatchesMenuIdValue() {
        UserService userService = mock(UserService.class);
        MenuService menuService = mock(MenuService.class);
        List<Menu> menus = Arrays.asList(menu("menu-system", null, "系统管理"), menu("menu-user", "menu-system", "用户管理"));
        User currentUser = superUser();

        when(userService.getById(EntityId.of("user-1"))).thenReturn(currentUser);
        when(menuService.list(any(Menu.class))).thenReturn(menus);
        when(menuService.listByIds(anyList())).thenReturn(menus);
        mockApplicationContext(userService);
        UserAccessHolder.currentUserId("user-1", "token-1");

        PersonalController controller = new PersonalController(
                userService,
                mock(RoleService.class),
                menuService,
                mock(PasswordService.class),
                mock(KeypairService.class));

        List<PersonalMenuResponse> responses = controller.menus();

        assertEquals(2, responses.size());
        assertEquals("menu-system", responses.get(0).getId());
        assertEquals("menu-user", responses.get(1).getId());
        assertEquals("menu-system", responses.get(1).getParentId());
    }

    private void assertMapping(RequestMapping mapping, String value) {
        Assert.assertNotNull(mapping);
        assertEquals(1, mapping.value().length);
        assertEquals(value, mapping.value()[0]);
        if (!value.startsWith("/api/")) {
            assertEquals(1, mapping.method().length);
            assertEquals(RequestMethod.POST, mapping.method()[0]);
        }
    }

    private void mockApplicationContext(UserService userService) {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(UserService.class)).thenReturn(userService);
        SpringContextHolder.setApplicationContext(applicationContext);
    }

    private User superUser() {
        User user = new User();
        user.setId(EntityId.of("user-1"));
        user.setPrivilege(UserPrivilege.SUPER);
        return user;
    }

    private Menu menu(String id, String parentId, String name) {
        Menu menu = new Menu();
        menu.setId(EntityId.of(id));
        menu.setParentId(parentId);
        menu.setName(name);
        return menu;
    }
}
