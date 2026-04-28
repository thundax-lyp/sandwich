package com.github.thundax.modules.auth.security;

import com.github.thundax.autoconfigure.LoginProperties;
import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.Constants;
import com.github.thundax.common.config.Global;
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
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.utils.MenuServiceHolder;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
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

        permissionService = new PermissionServiceImpl(permissionDao, authProperties);
        authService = new AuthServiceImpl(
                authProperties,
                new LoginProperties(),
                new InMemoryLoginFormDaoImpl(),
                accessTokenDao,
                new InMemoryLoginLockDaoImpl(),
                new PlainPasswordService(),
                permissionService);

        new UserServiceHolder(new TestUserService());
        new MenuServiceHolder(new TestMenuService());
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
                new VltavaProperties.AccessTokenFilterProperties(), authService, permissionService);

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
                new VltavaProperties.AccessTokenFilterProperties(), authService, permissionService);

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
        public int updateEnableFlag(User user) {
            return 1;
        }

        @Override
        public int updateEnableFlag(List<User> list) {
            return list.size();
        }

        @Override
        public List<com.github.thundax.modules.sys.entity.Role> findUserRole(User user) {
            return Collections.emptyList();
        }

        @Override
        public Class<User> getElementType() {
            return User.class;
        }

        @Override
        public User newEntity(String id) {
            return new User(id);
        }

        @Override
        public User get(User entity) {
            return user();
        }

        @Override
        public User get(String id) {
            return user();
        }

        @Override
        public List<User> getMany(List<String> ids) {
            return Collections.singletonList(user());
        }

        @Override
        public List<User> findList(User entity) {
            return Collections.singletonList(user());
        }

        @Override
        public User findOne(User entity) {
            return user();
        }

        @Override
        public Page<User> findPage(User entity, Page<User> page) {
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
        public int delete(User entity) {
            return 1;
        }

        @Override
        public int delete(List<User> list) {
            return list.size();
        }

        @Override
        public int updatePriority(User entity) {
            return 1;
        }

        @Override
        public int updatePriority(List<User> list) {
            return list.size();
        }

        @Override
        public int updateDelFlag(User entity) {
            return 1;
        }

        @Override
        public int updateDelFlag(List<User> list) {
            return list.size();
        }

        private User user() {
            User user = new User("u1");
            user.setLoginName("tester");
            user.setLoginPass("secret");
            user.setEnableFlag(Global.YES);
            user.setAdminFlag(Global.YES);
            user.setSuperFlag(Global.YES);
            user.setRanks(0);
            return user;
        }
    }

    private static class TestMenuService implements MenuService {

        @Override
        public List<Menu> findList(Integer maxRank) {
            return menus();
        }

        @Override
        public List<Menu> findChildList(String parentId) {
            return Collections.emptyList();
        }

        @Override
        public int updateDisplayFlag(Menu menu) {
            return 1;
        }

        @Override
        public int updateDisplayFlag(List<Menu> list) {
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
            return new Menu(id);
        }

        @Override
        public Menu get(Menu entity) {
            return menus().get(0);
        }

        @Override
        public Menu get(String id) {
            return menus().get(0);
        }

        @Override
        public List<Menu> getMany(List<String> ids) {
            return menus();
        }

        @Override
        public List<Menu> findList(Menu entity) {
            return menus();
        }

        @Override
        public Menu findOne(Menu entity) {
            return menus().get(0);
        }

        @Override
        public Page<Menu> findPage(Menu entity, Page<Menu> page) {
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
        public int delete(Menu entity) {
            return 1;
        }

        @Override
        public int delete(List<Menu> list) {
            return list.size();
        }

        @Override
        public int updatePriority(Menu entity) {
            return 1;
        }

        @Override
        public int updatePriority(List<Menu> list) {
            return list.size();
        }

        @Override
        public int updateDelFlag(Menu entity) {
            return 1;
        }

        @Override
        public int updateDelFlag(List<Menu> list) {
            return list.size();
        }

        private List<Menu> menus() {
            Menu menu = new Menu("m1");
            menu.setPerms("sys:role,sys:user:view");
            menu.setName("system");
            menu.setRanks(0);
            return Arrays.asList(menu);
        }
    }
}
