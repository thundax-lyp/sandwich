package com.github.thundax.modules.auth.security;

import com.github.thundax.autoconfigure.LoginProperties;
import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.Constants;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.auth.config.AuthProperties;
import com.github.thundax.modules.auth.entity.AccessToken;
import com.github.thundax.modules.auth.security.filter.AccessTokenAuthenticationFilter;
import com.github.thundax.modules.auth.service.AuthService;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.service.PermissionService;
import com.github.thundax.modules.auth.service.impl.AuthServiceImpl;
import com.github.thundax.modules.auth.service.impl.PermissionServiceImpl;
import com.github.thundax.modules.auth.testsupport.InMemoryAccessTokenDaoImpl;
import com.github.thundax.modules.auth.testsupport.InMemoryLoginFormDaoImpl;
import com.github.thundax.modules.auth.testsupport.InMemoryLoginLockDaoImpl;
import com.github.thundax.modules.auth.testsupport.InMemoryPermissionDaoImpl;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthPermissionLifecycleTest {

    private InMemoryAccessTokenDaoImpl accessTokenDao;
    private InMemoryPermissionDaoImpl permissionDao;
    private AuthService authService;
    private PermissionService permissionService;

    @Before
    public void setUp() {
        accessTokenDao = new InMemoryAccessTokenDaoImpl();
        permissionDao = new InMemoryPermissionDaoImpl();

        AuthProperties authProperties = new AuthProperties();
        authProperties.setLoginExpiredSeconds(60);

        permissionService = new PermissionServiceImpl(
                permissionDao, authProperties, new TestUserService(), new TestRoleService(), new TestMenuService());
        authService = new AuthServiceImpl(
                authProperties,
                new LoginProperties(),
                new InMemoryLoginFormDaoImpl(),
                accessTokenDao,
                new InMemoryLoginLockDaoImpl(),
                new PlainPasswordService(),
                permissionService);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldCreateTouchAndReleasePermissionSessionWithAccessToken() {
        AccessToken accessToken = authService.createAccessToken("u1");

        Assert.assertNotNull(permissionService.getSession(accessToken.getToken()));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "sys:role:view"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "user"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "admin"));
        Assert.assertTrue(permissionService.isPermitted(accessToken.getToken(), "super"));

        authService.activeAccessToken(accessToken);
        Assert.assertTrue(permissionDao.getTouchCount() > 0);

        authService.deleteAccessToken(accessToken);
        Assert.assertNull(permissionService.getSession(accessToken.getToken()));
    }

    @Test
    public void shouldAuthenticateRequestAndPopulateSpringSecurityContext() throws Exception {
        AccessToken accessToken = authService.createAccessToken("u1");
        AccessTokenAuthenticationFilter filter = new AccessTokenAuthenticationFilter(
                new VltavaProperties.AccessTokenFilterProperties(),
                authService,
                permissionService,
                new TestUserService());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sys/user");
        request.addHeader(Constants.HEADER_TOKEN, accessToken.getToken());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Assert.assertEquals(
                "u1", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Assert.assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        Assert.assertTrue(permissionDao.getTouchCount() > 0);
        Assert.assertTrue(accessTokenDao.getActiveCount() > 0);
    }

    @Test
    public void shouldRejectRequestWithoutToken() throws Exception {
        AccessTokenAuthenticationFilter filter = new AccessTokenAuthenticationFilter(
                new VltavaProperties.AccessTokenFilterProperties(),
                authService,
                permissionService,
                new TestUserService());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sys/user");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getContentAsString().contains("未授权用户"));
        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private static class PlainPasswordService implements PasswordService {

        @Override
        public String encrypt(String plainPassword) {
            return plainPassword;
        }

        @Override
        public boolean validate(String plainPassword, String encryptedPassword) {
            return plainPassword != null && plainPassword.equals(encryptedPassword);
        }
    }

    private static class TestUserService implements UserService {

        @Override
        public User getByLoginName(String loginName) {
            return user();
        }

        @Override
        public User getBySsoLoginName(String ssoLoginName) {
            return user();
        }

        @Override
        public void updatePassword(User user) {}

        @Override
        public void updateLoginInfo(User user) {}

        @Override
        public int updateStatus(User user) {
            return 1;
        }

        @Override
        public int updateStatus(List<User> list) {
            return list.size();
        }

        @Override
        public List<com.github.thundax.modules.sys.entity.Role> listUserRoles(User user) {
            return Collections.emptyList();
        }

        @Override
        public Class<User> getElementType() {
            return User.class;
        }

        @Override
        public User newEntity(String id) {
            User user = new User();
            user.setId(EntityIdCodec.toDomain(id));
            return user;
        }

        @Override
        public User getById(User entity) {
            return user();
        }

        @Override
        public User getById(EntityId id) {
            return user();
        }

        @Override
        public List<User> batchGetByIds(List<EntityId> ids) {
            return Collections.singletonList(user());
        }

        @Override
        public List<User> list(User entity) {
            return Collections.singletonList(user());
        }

        @Override
        public List<User> list(UserQuery query) {
            return Collections.singletonList(user());
        }

        @Override
        public User getOne(User entity) {
            return user();
        }

        @Override
        public Page<User> page(User entity, Page<User> page) {
            return page;
        }

        @Override
        public Page<User> page(UserQuery query, Page<User> page) {
            return page;
        }

        @Override
        public long count(User entity) {
            return 1;
        }

        @Override
        public void add(User entity) {}

        @Override
        public void update(User entity) {}

        @Override
        public int deleteById(EntityId id) {
            return 1;
        }

        @Override
        public int batchDeleteById(List<EntityId> ids) {
            return ids.size();
        }

        @Override
        public int updatePriority(User entity) {
            return 1;
        }

        @Override
        public int updatePriority(List<User> list) {
            return list.size();
        }

        private User user() {
            User user = new User();
            user.setId(EntityIdCodec.toDomain("u1"));
            user.setLoginName("tester");
            user.setLoginPass("secret");
            user.setStatus(UserStatus.ENABLED);
            user.setPrivilege(UserPrivilege.SUPER);
            user.setRanks(0);
            return user;
        }
    }

    private static class TestMenuService implements MenuService {

        @Override
        public List<Menu> list(Integer maxRank) {
            return menus();
        }

        @Override
        public List<Menu> listChildren(String parentId) {
            return Collections.emptyList();
        }

        @Override
        public int updateVisibility(Menu menu) {
            return 1;
        }

        @Override
        public int updateVisibility(List<Menu> list) {
            return list.size();
        }

        @Override
        public void moveTreeNode(Menu fromBean, Menu toBean, MoveTreeNodeType moveType) {}

        @Override
        public boolean isChildOf(Menu child, Menu parent) {
            return false;
        }

        @Override
        public Class<Menu> getElementType() {
            return Menu.class;
        }

        @Override
        public Menu newEntity(String id) {
            Menu menu = new Menu();
            menu.setId(EntityIdCodec.toDomain(id));
            return menu;
        }

        @Override
        public Menu getById(Menu entity) {
            return menus().get(0);
        }

        @Override
        public Menu getById(EntityId id) {
            return menus().get(0);
        }

        @Override
        public List<Menu> batchGetByIds(List<EntityId> ids) {
            return menus();
        }

        @Override
        public List<Menu> list(Menu entity) {
            return menus();
        }

        @Override
        public List<Menu> list(MenuQuery query) {
            return menus();
        }

        @Override
        public Menu getOne(Menu entity) {
            return menus().get(0);
        }

        @Override
        public Page<Menu> page(Menu entity, Page<Menu> page) {
            return page;
        }

        @Override
        public Page<Menu> page(MenuQuery query, Page<Menu> page) {
            return page;
        }

        @Override
        public long count(Menu entity) {
            return menus().size();
        }

        @Override
        public void add(Menu entity) {}

        @Override
        public void update(Menu entity) {}

        @Override
        public int deleteById(EntityId id) {
            return 1;
        }

        @Override
        public int batchDeleteById(List<EntityId> ids) {
            return ids.size();
        }

        @Override
        public int updatePriority(Menu entity) {
            return 1;
        }

        @Override
        public int updatePriority(List<Menu> list) {
            return list.size();
        }

        private List<Menu> menus() {
            Menu menu = new Menu();
            menu.setId(EntityIdCodec.toDomain("m1"));
            menu.setPerms("sys:role,sys:user:view");
            menu.setName("system");
            menu.setRanks(0);
            return Arrays.asList(menu);
        }
    }

    private static class TestRoleService implements RoleService {

        @Override
        public List<com.github.thundax.modules.sys.entity.Role> listEnabled() {
            return Collections.emptyList();
        }

        @Override
        public int updateStatus(com.github.thundax.modules.sys.entity.Role role) {
            return 1;
        }

        @Override
        public int updateStatus(List<com.github.thundax.modules.sys.entity.Role> list) {
            return list.size();
        }

        @Override
        public void updateUserList(com.github.thundax.modules.sys.entity.Role role, List<User> userList) {}

        @Override
        public List<User> listRoleUsers(com.github.thundax.modules.sys.entity.Role role) {
            return Collections.emptyList();
        }

        @Override
        public List<Menu> listRoleMenus(com.github.thundax.modules.sys.entity.Role role) {
            return Collections.emptyList();
        }

        @Override
        public Class<com.github.thundax.modules.sys.entity.Role> getElementType() {
            return com.github.thundax.modules.sys.entity.Role.class;
        }

        @Override
        public com.github.thundax.modules.sys.entity.Role newEntity(String id) {
            com.github.thundax.modules.sys.entity.Role role = new com.github.thundax.modules.sys.entity.Role();
            role.setId(EntityIdCodec.toDomain(id));
            return role;
        }

        @Override
        public com.github.thundax.modules.sys.entity.Role getById(com.github.thundax.modules.sys.entity.Role entity) {
            return entity;
        }

        @Override
        public com.github.thundax.modules.sys.entity.Role getById(EntityId id) {
            return newEntity(id.value());
        }

        @Override
        public List<com.github.thundax.modules.sys.entity.Role> batchGetByIds(List<EntityId> ids) {
            return Collections.emptyList();
        }

        @Override
        public List<com.github.thundax.modules.sys.entity.Role> list(
                com.github.thundax.modules.sys.entity.Role entity) {
            return Collections.emptyList();
        }

        @Override
        public List<com.github.thundax.modules.sys.entity.Role> list(RoleQuery query) {
            return Collections.emptyList();
        }

        @Override
        public com.github.thundax.modules.sys.entity.Role getOne(com.github.thundax.modules.sys.entity.Role entity) {
            return entity;
        }

        @Override
        public Page<com.github.thundax.modules.sys.entity.Role> page(
                com.github.thundax.modules.sys.entity.Role entity,
                Page<com.github.thundax.modules.sys.entity.Role> page) {
            return page;
        }

        @Override
        public Page<com.github.thundax.modules.sys.entity.Role> page(
                RoleQuery query, Page<com.github.thundax.modules.sys.entity.Role> page) {
            return page;
        }

        @Override
        public long count(com.github.thundax.modules.sys.entity.Role entity) {
            return 0;
        }

        @Override
        public void add(com.github.thundax.modules.sys.entity.Role entity) {}

        @Override
        public void update(com.github.thundax.modules.sys.entity.Role entity) {}

        @Override
        public int deleteById(EntityId id) {
            return 1;
        }

        @Override
        public int batchDeleteById(List<EntityId> ids) {
            return ids.size();
        }

        @Override
        public int updatePriority(com.github.thundax.modules.sys.entity.Role entity) {
            return 1;
        }

        @Override
        public int updatePriority(List<com.github.thundax.modules.sys.entity.Role> list) {
            return list.size();
        }
    }
}
