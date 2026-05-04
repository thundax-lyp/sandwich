package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.RoleDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class RoleServiceImplTest {

    @Test
    public void shouldExpandFindListQuery() {
        RecordingRoleDao dao = new RecordingRoleDao();
        RoleQuery query = new RoleQuery();
        query.setStatus(RoleStatus.ENABLED);
        RoleServiceImpl service = new RoleServiceImpl(dao, new RecordingSignService());

        service.list(query);

        assertEquals("ENABLED", dao.enableFlag);
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingRoleDao dao = new RecordingRoleDao();
        PageDTO<Role> page = new PageDTO<>();
        page.setPageNo(0);
        page.setPageSize(0);
        RoleServiceImpl service = new RoleServiceImpl(dao, new RecordingSignService());

        service.page(new RoleQuery(), page);

        assertEquals(PageRules.firstPageIndex(), dao.pageNo);
        assertEquals(PageRules.defaultPageSize(), dao.pageSize);
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
        assertEquals(EntityIdCodec.toValue(role.getId()), dao.deletedRoleMenuId);
        assertEquals(Arrays.asList("m1", "m2"), dao.menuIdList);
        assertEquals(Role.BEAN_NAME, signService.businessType);
    }

    @Test
    public void shouldUpdateUserListByIds() {
        RecordingRoleDao dao = new RecordingRoleDao();
        Role role = role("role-1");
        role.setMenuIdList(Arrays.asList());
        RoleServiceImpl service = new RoleServiceImpl(dao, new RecordingSignService());

        service.updateUserList(role, Arrays.asList(user("u1"), user("u2")));

        assertEquals("role-1", dao.deletedRoleUserId);
        assertEquals(Arrays.asList("u1", "u2"), dao.userIdList);
    }

    @Test
    public void shouldDeleteRoleRelationsBeforeRole() {
        RecordingRoleDao dao = new RecordingRoleDao();
        dao.getResult = role("role-1");
        RoleServiceImpl service = new RoleServiceImpl(dao, new RecordingSignService());

        int count = service.deleteById(EntityId.of("role-1"));

        assertEquals(1, count);
        assertEquals("role-1", dao.deletedRoleMenuId);
        assertEquals("role-1", dao.deletedRoleUserId);
        assertEquals("role-1", dao.deletedRoleId);
    }

    private static Role role(String id) {
        Role role = new Role();
        role.setId(EntityIdCodec.toDomain(id));
        return role;
    }

    private static User user(String id) {
        User user = new User();
        user.setId(EntityIdCodec.toDomain(id));
        return user;
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
        private Role getResult;

        @Override
        public Role getById(EntityId id) {
            return getResult;
        }

        @Override
        public List<Role> listByIds(List<String> idList) {
            return null;
        }

        @Override
        public List<Role> list(String enableFlag) {
            this.enableFlag = enableFlag;
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Role> page(
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
        public String insert(Role role) {
            this.inserted = role;
            return "generated-role-id";
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
        public int deleteById(EntityId id) {
            this.deletedRoleId = id.value();
            return 1;
        }

        @Override
        public int updateStatus(Role role) {
            return 1;
        }

        @Override
        public List<String> listRoleMenus(String roleId) {
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
        public List<String> listRoleUsers(String roleId) {
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
