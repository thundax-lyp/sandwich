package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.dao.DepartmentDao;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import java.util.List;
import org.junit.Test;

public class DepartmentServiceImplTest {

    @Test
    public void shouldIgnoreBlankId() {
        RecordingDepartmentDao dao = new RecordingDepartmentDao();
        DepartmentServiceImpl service = new DepartmentServiceImpl(dao);

        assertEquals(null, service.getById((EntityId) null));
        assertEquals(0, dao.getCalls);
    }

    @Test
    public void shouldExpandFindListQuery() {
        RecordingDepartmentDao dao = new RecordingDepartmentDao();
        DepartmentQuery query = new DepartmentQuery();
        query.setParentId(0L);
        query.setName("总部");
        query.setRemarks("备注");
        DepartmentServiceImpl service = new DepartmentServiceImpl(dao);

        service.list(query);

        assertEquals(Long.valueOf(0L), dao.parentId);
        assertEquals("总部", dao.name);
        assertEquals("备注", dao.remarks);
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingDepartmentDao dao = new RecordingDepartmentDao();
        PageDTO<Department> page = new PageDTO<>();
        page.setPageNo(0);
        page.setPageSize(0);
        DepartmentServiceImpl service = new DepartmentServiceImpl(dao);

        service.page(new DepartmentQuery(), page);

        assertEquals(PageRules.firstPageIndex(), dao.pageNo);
        assertEquals(PageRules.defaultPageSize(), dao.pageSize);
        assertEquals(1L, page.getCount());
    }

    @Test
    public void shouldPrepareDepartmentBeforeInsert() {
        RecordingDepartmentDao dao = new RecordingDepartmentDao();
        Department department = new Department();
        DepartmentServiceImpl service = new DepartmentServiceImpl(dao);

        service.add(department);

        assertNotNull(department.getId());
        assertEquals(null, department.getCreateDate());
        assertSame(department, dao.inserted);
    }

    @Test
    public void shouldPrepareDepartmentBeforeUpdate() {
        RecordingDepartmentDao dao = new RecordingDepartmentDao();
        Department department = department(6001L);
        DepartmentServiceImpl service = new DepartmentServiceImpl(dao);

        service.update(department);

        assertEquals(null, department.getUpdateDate());
        assertSame(department, dao.updated);
    }

    @Test
    public void shouldDeleteStoredDepartment() {
        RecordingDepartmentDao dao = new RecordingDepartmentDao();
        dao.getResult = department(6001L);
        DepartmentServiceImpl service = new DepartmentServiceImpl(dao);

        int count = service.deleteById(EntityId.of(6001L));

        assertEquals(1, count);
        assertEquals(Long.valueOf(6001L), dao.deletedId);
    }

    private static Department department(Long id) {
        Department department = new Department();
        department.setId(EntityIdCodec.toDomain(id));
        return department;
    }

    private static class RecordingDepartmentDao implements DepartmentDao {

        private Department getResult;
        private int getCalls;
        private Long parentId;
        private String name;
        private String remarks;
        private int pageNo;
        private int pageSize;
        private Department inserted;
        private Department updated;
        private Long deletedId;
        private int priorityCalls;

        @Override
        public Department getById(EntityId id) {
            this.getCalls++;
            return getResult;
        }

        @Override
        public List<Department> listByIds(List<Long> idList) {
            return null;
        }

        @Override
        public List<Department> list(Long parentId, String name, String remarks) {
            this.parentId = parentId;
            this.name = name;
            this.remarks = remarks;
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Department> page(
                Long parentId, String name, String remarks, int pageNo, int pageSize) {
            this.parentId = parentId;
            this.name = name;
            this.remarks = remarks;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Department> dataPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
            dataPage.setTotal(1);
            return dataPage;
        }

        @Override
        public EntityId insert(Department department) {
            this.inserted = department;
            return EntityId.of(9006L);
        }

        @Override
        public int update(Department department) {
            this.updated = department;
            return 1;
        }

        @Override
        public int updatePriority(Department department) {
            this.priorityCalls++;
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
    }
}
