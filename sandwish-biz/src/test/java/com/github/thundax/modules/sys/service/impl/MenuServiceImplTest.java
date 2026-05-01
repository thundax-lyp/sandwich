package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class MenuServiceImplTest {

    @Test
    public void shouldIgnoreBlankId() {
        RecordingMenuDao dao = new RecordingMenuDao();
        MenuServiceImpl service = new MenuServiceImpl(dao, new RecordingSignService());

        assertEquals(null, service.get((EntityId) null));
        assertEquals(0, dao.getCalls);
    }

    @Test
    public void shouldExpandFindListQuery() {
        RecordingMenuDao dao = new RecordingMenuDao();
        Menu menu = new Menu();
        Menu.Query query = new Menu.Query();
        query.setParentId("ROOT");
        query.setVisibility(MenuVisibility.VISIBLE);
        query.setMaxRank(3);
        menu.setQuery(query);
        MenuServiceImpl service = new MenuServiceImpl(dao, new RecordingSignService());

        service.findList(menu);

        assertEquals("ROOT", dao.parentId);
        assertEquals("VISIBLE", dao.displayFlag);
        assertEquals(Integer.valueOf(3), dao.maxRank);
    }

    @Test
    public void shouldUseDaoFiltersForConvenienceQueries() {
        RecordingMenuDao dao = new RecordingMenuDao();
        MenuServiceImpl service = new MenuServiceImpl(dao, new RecordingSignService());

        service.findList(2);
        assertEquals(Integer.valueOf(2), dao.maxRank);

        service.findChildList("parent-1");
        assertEquals("parent-1", dao.parentId);
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingMenuDao dao = new RecordingMenuDao();
        Page<Menu> page = new Page<>();
        page.setPageNo(0);
        page.setPageSize(0);
        MenuServiceImpl service = new MenuServiceImpl(dao, new RecordingSignService());

        service.findPage(new Menu(), page);

        assertEquals(Page.FIRST_PAGE_INDEX, dao.pageNo);
        assertEquals(Page.DEFAULT_PAGE_SIZE, dao.pageSize);
        assertEquals(1L, page.getCount());
    }

    @Test
    public void shouldPrepareAndSignMenuBeforeInsert() {
        RecordingMenuDao dao = new RecordingMenuDao();
        RecordingSignService signService = new RecordingSignService();
        Menu menu = new Menu();
        MenuServiceImpl service = new MenuServiceImpl(dao, signService);

        service.add(menu);

        assertNotNull(menu.getId());
        assertEquals(null, menu.getCreateDate());
        assertSame(menu, dao.inserted);
        assertEquals(Menu.BEAN_NAME, signService.businessType);
        assertEquals(menu.getSignId(), signService.businessId);
    }

    @Test
    public void shouldPrepareAndSignMenuBeforeUpdate() {
        RecordingMenuDao dao = new RecordingMenuDao();
        RecordingSignService signService = new RecordingSignService();
        Menu menu = menu("menu-1");
        MenuServiceImpl service = new MenuServiceImpl(dao, signService);

        service.update(menu);

        assertEquals(null, menu.getUpdateDate());
        assertSame(menu, dao.updated);
        assertEquals(Menu.BEAN_NAME, signService.businessType);
    }

    @Test
    public void shouldDeleteMenuRoleBeforeDeletingMenu() {
        RecordingMenuDao dao = new RecordingMenuDao();
        Menu stored = menu("menu-1");
        dao.getResult = stored;
        RecordingSignService signService = new RecordingSignService();
        MenuServiceImpl service = new MenuServiceImpl(dao, signService);

        int count = service.delete(menu("menu-1"));

        assertEquals(1, count);
        assertEquals("menu-1", dao.deletedMenuRoleId);
        assertEquals("menu-1", dao.deletedId);
        assertEquals(Menu.BEAN_NAME, signService.deletedBusinessType);
    }

    @Test
    public void shouldBatchUpdateDisplayFlag() {
        RecordingMenuDao dao = new RecordingMenuDao();
        MenuServiceImpl service = new MenuServiceImpl(dao, new RecordingSignService());

        int count = service.updateVisibility(Arrays.asList(menu("m1"), menu("m2")));

        assertEquals(2, count);
        assertEquals(2, dao.displayFlagCalls);
    }

    private static Menu menu(String id) {
        Menu menu = new Menu();
        menu.setId(EntityIdCodec.toDomain(id));
        return menu;
    }

    private static class RecordingMenuDao implements MenuDao {

        private Menu getResult;
        private int getCalls;
        private String parentId;
        private String displayFlag;
        private Integer maxRank;
        private Menu inserted;
        private Menu updated;
        private String deletedMenuRoleId;
        private String deletedId;
        private int displayFlagCalls;
        private int pageNo;
        private int pageSize;

        @Override
        public Menu get(EntityId id) {
            this.getCalls++;
            return getResult;
        }

        @Override
        public List<Menu> getMany(List<String> idList) {
            return null;
        }

        @Override
        public List<Menu> findList(String parentId, String displayFlag, Integer maxRank) {
            this.parentId = parentId;
            this.displayFlag = displayFlag;
            this.maxRank = maxRank;
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Menu> findPage(
                String parentId, String displayFlag, Integer maxRank, int pageNo, int pageSize) {
            this.parentId = parentId;
            this.displayFlag = displayFlag;
            this.maxRank = maxRank;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Menu> dataPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
            dataPage.setTotal(1);
            return dataPage;
        }

        @Override
        public String insert(Menu menu) {
            this.inserted = menu;
            return "generated-menu-id";
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
        public int delete(EntityId id) {
            this.deletedId = id.value();
            return 1;
        }

        @Override
        public void moveTreeNode(String fromId, String toId, TreeService.MoveTreeNodeType moveType) {}

        @Override
        public boolean isChildOf(String childId, String parentId) {
            return false;
        }

        @Override
        public int updateVisibility(Menu menu) {
            this.displayFlagCalls++;
            return 1;
        }

        @Override
        public void deleteMenuRole(String menuId) {
            this.deletedMenuRoleId = menuId;
        }
    }

    private static class RecordingSignService implements SignService {

        private String businessType;
        private String businessId;
        private String deletedBusinessType;

        @Override
        public Boolean sign(String businessType, String businessId, String body) {
            this.businessType = businessType;
            this.businessId = businessId;
            return true;
        }

        @Override
        public Boolean verifySign(String businessType, String businessId, String body) {
            return true;
        }

        @Override
        public void deleteSign(String businessType, String businessId) {
            this.deletedBusinessType = businessType;
        }
    }
}
