package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.dao.OfficeDao;
import com.github.thundax.modules.sys.entity.Office;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class OfficeServiceImplTest {

    @Test
    public void shouldIgnoreBlankId() {
        RecordingOfficeDao dao = new RecordingOfficeDao();
        OfficeServiceImpl service = new OfficeServiceImpl(dao);

        assertEquals(null, service.get(""));
        assertEquals(0, dao.getCalls);
    }

    @Test
    public void shouldExpandFindListQuery() {
        RecordingOfficeDao dao = new RecordingOfficeDao();
        Office office = new Office();
        Office.Query query = new Office.Query();
        query.setParentId("ROOT");
        query.setName("总部");
        query.setRemarks("备注");
        office.setQuery(query);
        OfficeServiceImpl service = new OfficeServiceImpl(dao);

        service.findList(office);

        assertEquals("ROOT", dao.parentId);
        assertEquals("总部", dao.name);
        assertEquals("备注", dao.remarks);
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingOfficeDao dao = new RecordingOfficeDao();
        Page<Office> page = new Page<>();
        page.setPageNo(0);
        page.setPageSize(0);
        OfficeServiceImpl service = new OfficeServiceImpl(dao);

        service.findPage(new Office(), page);

        assertEquals(Page.FIRST_PAGE_INDEX, dao.pageNo);
        assertEquals(Page.DEFAULT_PAGE_SIZE, dao.pageSize);
        assertEquals(1L, page.getCount());
    }

    @Test
    public void shouldPrepareOfficeBeforeInsert() {
        RecordingOfficeDao dao = new RecordingOfficeDao();
        Office office = new Office();
        OfficeServiceImpl service = new OfficeServiceImpl(dao);

        service.add(office);

        assertNotNull(office.getId());
        assertNotNull(office.getCreateDate());
        assertSame(office, dao.inserted);
    }

    @Test
    public void shouldPrepareOfficeBeforeUpdate() {
        RecordingOfficeDao dao = new RecordingOfficeDao();
        Office office = new Office("office-1");
        OfficeServiceImpl service = new OfficeServiceImpl(dao);

        service.update(office);

        assertNotNull(office.getUpdateDate());
        assertSame(office, dao.updated);
    }

    @Test
    public void shouldDeleteStoredOffice() {
        RecordingOfficeDao dao = new RecordingOfficeDao();
        dao.getResult = new Office("office-1");
        OfficeServiceImpl service = new OfficeServiceImpl(dao);

        int count = service.delete(new Office("office-1"));

        assertEquals(1, count);
        assertEquals("office-1", dao.deletedId);
    }

    @Test
    public void shouldBatchUpdatePriority() {
        RecordingOfficeDao dao = new RecordingOfficeDao();
        OfficeServiceImpl service = new OfficeServiceImpl(dao);

        int count = service.updatePriority(Arrays.asList(new Office("o1"), new Office("o2")));

        assertEquals(2, count);
        assertEquals(2, dao.priorityCalls);
    }

    private static class RecordingOfficeDao implements OfficeDao {

        private Office getResult;
        private int getCalls;
        private String parentId;
        private String name;
        private String remarks;
        private int pageNo;
        private int pageSize;
        private Office inserted;
        private Office updated;
        private String deletedId;
        private int priorityCalls;

        @Override
        public Office get(String id) {
            this.getCalls++;
            return getResult;
        }

        @Override
        public List<Office> getMany(List<String> idList) {
            return null;
        }

        @Override
        public List<Office> findList(String parentId, String name, String remarks) {
            this.parentId = parentId;
            this.name = name;
            this.remarks = remarks;
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Office> findPage(
                String parentId, String name, String remarks, int pageNo, int pageSize) {
            this.parentId = parentId;
            this.name = name;
            this.remarks = remarks;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Office> dataPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
            dataPage.setTotal(1);
            return dataPage;
        }

        @Override
        public String insert(Office office) {
            this.inserted = office;
            return "generated-office-id";
        }

        @Override
        public int update(Office office) {
            this.updated = office;
            return 1;
        }

        @Override
        public int updatePriority(Office office) {
            this.priorityCalls++;
            return 1;
        }

        @Override
        public int delete(String id) {
            this.deletedId = id;
            return 1;
        }

        @Override
        public void moveTreeNode(String fromId, String toId, TreeService.MoveTreeNodeType moveType) {}

        @Override
        public boolean isChildOf(String childId, String parentId) {
            return false;
        }
    }
}
