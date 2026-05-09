package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
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
        RoleServiceImpl service = new RoleServiceImpl(dao);

        service.list(query);

        assertEquals("ENABLED", dao.status);
    }

    @Test
    public void shouldListEnabledRolesWithEnabledStatus() {
        RecordingRoleDao dao = new RecordingRoleDao();
        RoleServiceImpl service = new RoleServiceImpl(dao);

        service.listEnabled();

        assertEquals("ENABLED", dao.status);
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingRoleDao dao = new RecordingRoleDao();
        PageDTO<Role> page = new PageDTO<>();
        page.setPageNo(0);
        page.setPageSize(0);
        RoleServiceImpl service = new RoleServiceImpl(dao);

        service.page(new RoleQuery(), page);

        assertEquals(PageRules.firstPageIndex(), dao.pageNo);
        assertEquals(PageRules.defaultPageSize(), dao.pageSize);
        assertEquals(1L, page.getCount());
    }

    @Test
    public void shouldSaveRoleMenus() {
        RecordingRoleDao dao = new RecordingRoleDao();
        Role role = new Role();
        role.setMenuIdList(Arrays.asList(5001L, 5002L));
        RoleServiceImpl service = new RoleServiceImpl(dao);

        service.add(role);

        assertNotNull(role.getId());
        assertSame(role, dao.inserted);
        assertEquals(EntityIdCodec.toValue(role.getId()), dao.deletedRoleMenuId);
        assertEquals(Arrays.asList(5001L, 5002L), dao.menuIdList);
    }

    @Test
    public void shouldUpdateUserListByIds() {
        RecordingRoleDao dao = new RecordingRoleDao();
        Role role = role(4001L);
        role.setMenuIdList(Arrays.asList());
        RoleServiceImpl service = new RoleServiceImpl(dao);

        service.updateUserList(role, Arrays.asList(user(1001L), user(1002L)));

        assertEquals(Long.valueOf(4001L), dao.deletedRoleUserId);
        assertEquals(Arrays.asList(1001L, 1002L), dao.userIdList);
    }

    @Test
    public void shouldDeleteRoleRelationsBeforeRole() {
        RecordingRoleDao dao = new RecordingRoleDao();
        dao.getResult = role(4001L);
        RoleServiceImpl service = new RoleServiceImpl(dao);

        int count = service.deleteById(EntityId.of(4001L));

        assertEquals(1, count);
        assertEquals(Long.valueOf(4001L), dao.deletedRoleMenuId);
        assertEquals(Long.valueOf(4001L), dao.deletedRoleUserId);
        assertEquals(Long.valueOf(4001L), dao.deletedRoleId);
    }

    private static Role role(Long id) {
        Role role = new Role();
        role.setId(EntityIdCodec.toDomain(id));
        return role;
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(EntityIdCodec.toDomain(id));
        return user;
    }

    private static class RecordingRoleDao implements RoleDao {

        private String status;
        private int pageNo;
        private int pageSize;
        private Role inserted;
        private Long deletedRoleMenuId;
        private Long deletedRoleUserId;
        private Long deletedRoleId;
        private List<Long> menuIdList;
        private List<Long> userIdList;
        private Role getResult;

        @Override
        public Role getById(EntityId id) {
            return getResult;
        }

        @Override
        public List<Role> listByIds(List<Long> idList) {
            return null;
        }

        @Override
        public List<Role> list(String status) {
            this.status = status;
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Role> page(
                String status, int pageNo, int pageSize) {
            this.status = status;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Role> dataPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
            dataPage.setTotal(1);
            return dataPage;
        }

        @Override
        public EntityId insert(Role role) {
            this.inserted = role;
            return EntityId.of(9004L);
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
        public List<Long> listRoleMenus(Long roleId) {
            return null;
        }

        @Override
        public void deleteRoleMenu(Long roleId) {
            this.deletedRoleMenuId = roleId;
        }

        @Override
        public void insertRoleMenu(Long roleId, List<Long> menuIdList) {
            this.menuIdList = menuIdList;
        }

        @Override
        public List<Long> listRoleUsers(Long roleId) {
            return null;
        }

        @Override
        public void deleteRoleUser(Long roleId) {
            this.deletedRoleUserId = roleId;
        }

        @Override
        public void insertRoleUser(Long roleId, List<Long> userIdList) {
            this.userIdList = userIdList;
        }
    }
}
