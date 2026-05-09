package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class MenuServiceImplTest {

    @Test
    public void shouldIgnoreBlankId() {
        RecordingMenuDao dao = new RecordingMenuDao();
        MenuServiceImpl service = new MenuServiceImpl(dao);

        assertEquals(null, service.getById((EntityId) null));
        assertEquals(0, dao.getCalls);
    }

    @Test
    public void shouldExpandFindListQuery() {
        RecordingMenuDao dao = new RecordingMenuDao();
        MenuQuery query = new MenuQuery();
        query.setParentId(0L);
        query.setVisibility(MenuVisibility.VISIBLE);
        query.setMaxRank(AccessRank.of(3));
        MenuServiceImpl service = new MenuServiceImpl(dao);

        service.list(query);

        assertEquals(Long.valueOf(0L), dao.parentId);
        assertEquals("VISIBLE", dao.visibility);
        assertEquals(Integer.valueOf(3), dao.maxRank);
    }

    @Test
    public void shouldUseQueryForRankAndParentFilters() {
        RecordingMenuDao dao = new RecordingMenuDao();
        MenuServiceImpl service = new MenuServiceImpl(dao);

        MenuQuery query = new MenuQuery();
        query.setParentId(5000L);
        query.setMaxRank(AccessRank.of(2));
        service.list(query);

        assertEquals(Long.valueOf(5000L), dao.parentId);
        assertEquals(Integer.valueOf(2), dao.maxRank);
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingMenuDao dao = new RecordingMenuDao();
        PageQuery page = new PageQuery();
        page.setPageNo(0);
        page.setPageSize(0);
        MenuServiceImpl service = new MenuServiceImpl(dao);

        PageResult<Menu> result = service.page(new MenuQuery(), page);

        assertEquals(PageRules.firstPageIndex(), dao.pageNo);
        assertEquals(PageRules.defaultPageSize(), dao.pageSize);
        assertEquals(1L, result.getTotalCount());
    }

    @Test
    public void shouldPrepareMenuBeforeInsert() {
        RecordingMenuDao dao = new RecordingMenuDao();
        Menu menu = new Menu();
        MenuServiceImpl service = new MenuServiceImpl(dao);

        service.add(menu);

        assertNotNull(menu.getId());
        assertEquals(null, menu.getCreateDate());
        assertSame(menu, dao.inserted);
    }

    @Test
    public void shouldPrepareMenuBeforeUpdate() {
        RecordingMenuDao dao = new RecordingMenuDao();
        Menu menu = menu(5001L);
        MenuServiceImpl service = new MenuServiceImpl(dao);

        service.update(menu);

        assertEquals(null, menu.getUpdateDate());
        assertSame(menu, dao.updated);
    }

    @Test
    public void shouldDeleteMenuRoleBeforeDeletingMenu() {
        RecordingMenuDao dao = new RecordingMenuDao();
        Menu stored = menu(5001L);
        dao.getResult = stored;
        MenuServiceImpl service = new MenuServiceImpl(dao);

        int count = service.deleteById(EntityId.of(5001L));

        assertEquals(1, count);
        assertEquals(Long.valueOf(5001L), dao.deletedMenuRoleId);
        assertEquals(Long.valueOf(5001L), dao.deletedId);
    }

    @Test
    public void shouldBatchUpdateVisibility() {
        RecordingMenuDao dao = new RecordingMenuDao();
        MenuServiceImpl service = new MenuServiceImpl(dao);

        int count = service.batchUpdateVisibility(Arrays.asList(menu(5001L), menu(5002L)));

        assertEquals(2, count);
        assertEquals(2, dao.visibilityCalls);
    }

    private static Menu menu(Long id) {
        Menu menu = new Menu();
        menu.setId(EntityIdCodec.toDomain(id));
        return menu;
    }

    private static class RecordingMenuDao implements MenuDao {

        private Menu getResult;
        private int getCalls;
        private Long parentId;
        private String visibility;
        private Integer maxRank;
        private Menu inserted;
        private Menu updated;
        private Long deletedMenuRoleId;
        private Long deletedId;
        private int visibilityCalls;
        private int pageNo;
        private int pageSize;

        @Override
        public Menu getById(EntityId id) {
            this.getCalls++;
            return getResult;
        }

        @Override
        public List<Menu> listByIds(List<Long> idList) {
            return null;
        }

        @Override
        public List<Menu> list(Long parentId, String visibility, Integer maxRank) {
            this.parentId = parentId;
            this.visibility = visibility;
            this.maxRank = maxRank;
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Menu> page(
                Long parentId, String visibility, Integer maxRank, int pageNo, int pageSize) {
            this.parentId = parentId;
            this.visibility = visibility;
            this.maxRank = maxRank;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Menu> dataPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
            dataPage.setTotal(1);
            return dataPage;
        }

        @Override
        public EntityId insert(Menu menu) {
            this.inserted = menu;
            return EntityId.of(9005L);
        }

        @Override
        public int update(Menu menu) {
            this.updated = menu;
            return 1;
        }

        @Override
        public int updatePriority(Menu menu) {
            return 1;
        }

        @Override
        public int deleteById(EntityId id) {
            this.deletedId = id.value();
            return 1;
        }

        @Override
        public void moveTreeNode(Long fromId, Long toId, TreeNodeMoveType moveType) {}

        @Override
        public boolean isChildOf(Long childId, Long parentId) {
            return false;
        }

        @Override
        public int updateVisibility(Menu menu) {
            this.visibilityCalls++;
            return 1;
        }

        @Override
        public void deleteMenuRole(Long menuId) {
            this.deletedMenuRoleId = menuId;
        }
    }
}
