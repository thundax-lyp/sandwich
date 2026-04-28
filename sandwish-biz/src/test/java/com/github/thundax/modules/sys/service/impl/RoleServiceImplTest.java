package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.RoleDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class RoleServiceImplTest {

    @Test
    public void shouldExpandFindListQuery() {
        RecordingRoleDao dao = new RecordingRoleDao();
        Role role = new Role();
        Role.Query query = new Role.Query();
        query.setEnableFlag("1");
        role.setQuery(query);
        RoleServiceImpl service = new RoleServiceImpl(dao, new RecordingSignService());

        service.findList(role);

        assertEquals("1", dao.enableFlag);
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingRoleDao dao = new RecordingRoleDao();
        Page<Role> page = new Page<>();
        page.setPageNo(0);
        page.setPageSize(0);
        RoleServiceImpl service = new RoleServiceImpl(dao, new RecordingSignService());

        service.findPage(new Role(), page);

        assertEquals(Page.FIRST_PAGE_INDEX, dao.pageNo);
        assertEquals(Page.DEFAULT_PAGE_SIZE, dao.pageSize);
        assertEquals(1L, page.getCount());
    }

    @Test
    public void shouldSaveRoleMenusAndSign() {
        RecordingRoleDao dao = new RecordingRoleDao();
        RecordingSignService signService = new RecordingSignService();
        Role role = new Role();
        role.setMenuIdList(Arrays.asList("m1", "m2"));
        RoleServiceImpl service = new RoleServiceImpl(dao, signService);

        service.add(role);

        assertNotNull(role.getId());
        assertSame(role, dao.inserted);
        assertEquals(role.getId(), dao.deletedRoleMenuId);
        assertEquals(Arrays.asList("m1", "m2"), dao.menuIdList);
        assertEquals(Role.BEAN_NAME, signService.businessType);
    }

    @Test
    public void shouldUpdateUserListByIds() {
        RecordingRoleDao dao = new RecordingRoleDao();
        Role role = new Role("role-1");
        role.setMenuIdList(Arrays.asList());
        RoleServiceImpl service = new RoleServiceImpl(dao, new RecordingSignService());

        service.updateUserList(role, Arrays.asList(new User("u1"), new User("u2")));

        assertEquals("role-1", dao.deletedRoleUserId);
        assertEquals(Arrays.asList("u1", "u2"), dao.userIdList);
    }

    @Test
    public void shouldDeleteRoleRelationsBeforeRole() {
        RecordingRoleDao dao = new RecordingRoleDao();
        RoleServiceImpl service = new RoleServiceImpl(dao, new RecordingSignService());

        int count = service.delete(new Role("role-1"));

        assertEquals(1, count);
        assertEquals("role-1", dao.deletedRoleMenuId);
        assertEquals("role-1", dao.deletedRoleUserId);
        assertEquals("role-1", dao.deletedRoleId);
    }

    private static class RecordingRoleDao implements RoleDao {

        private String enableFlag;
        private int pageNo;
        private int pageSize;
        private Role inserted;
        private String deletedRoleMenuId;
        private String deletedRoleUserId;
        private String deletedRoleId;
        private List<String> menuIdList;
        private List<String> userIdList;

        @Override
        public Role get(String id) {
            return null;
        }

        @Override
        public List<Role> getMany(List<String> idList) {
            return null;
        }

        @Override
        public List<Role> findList(String enableFlag) {
            this.enableFlag = enableFlag;
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Role> findPage(
                String enableFlag, int pageNo, int pageSize) {
            this.enableFlag = enableFlag;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Role> dataPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
            dataPage.setTotal(1);
            return dataPage;
        }

        @Override
        public int insert(Role role) {
            this.inserted = role;
            return 1;
        }

        @Override
        public int update(Role role) {
            return 1;
        }

        @Override
        public int updatePriority(Role role) {
            return 1;
        }

        @Override
        public int updateDelFlag(Role role) {
            return 1;
        }

        @Override
        public int delete(String id) {
            this.deletedRoleId = id;
            return 1;
        }

        @Override
        public int updateEnableFlag(Role role) {
            return 1;
        }

        @Override
        public List<String> findRoleMenu(String roleId) {
            return null;
        }

        @Override
        public void deleteRoleMenu(String roleId) {
            this.deletedRoleMenuId = roleId;
        }

        @Override
        public void insertRoleMenu(String roleId, List<String> menuIdList) {
            this.menuIdList = menuIdList;
        }

        @Override
        public List<String> findRoleUser(String roleId) {
            return null;
        }

        @Override
        public void deleteRoleUser(String roleId) {
            this.deletedRoleUserId = roleId;
        }

        @Override
        public void insertRoleUser(String roleId, List<String> userIdList) {
            this.userIdList = userIdList;
        }
    }

    private static class RecordingSignService implements SignService {

        private String businessType;

        @Override
        public Boolean sign(String businessType, String businessId, String body) {
            this.businessType = businessType;
            return true;
        }

        @Override
        public Boolean verifySign(String businessType, String businessId, String body) {
            return true;
        }

        @Override
        public void deleteSign(String businessType, String businessId) {}
    }
}
