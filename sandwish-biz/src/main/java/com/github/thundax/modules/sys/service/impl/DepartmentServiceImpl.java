package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.sys.dao.DepartmentDao;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.command.ChangeDepartmentInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateDepartmentCommand;
import com.github.thundax.modules.sys.service.command.DeleteDepartmentCommand;
import com.github.thundax.modules.sys.service.command.MoveDepartmentCommand;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentDao dao;

    public DepartmentServiceImpl(DepartmentDao dao) {
        this.dao = dao;
    }

    public Department get(DepartmentId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<Department> list(DepartmentQuery query) {
        return dao.list(
                query == null ? null : DepartmentIdCodec.toValue(query.getParentId()),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks());
    }

    public PageResult<Department> page(DepartmentQuery query, PageQuery page) {
        PageQuery normalizedPage = normalizePage(page);
        IPage<Department> dataPage = dao.page(
                query == null ? null : DepartmentIdCodec.toValue(query.getParentId()),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @AuditLog(type = "Department", id = "", action = AuditAction.CREATE, summary = "创建部门", recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public DepartmentId create(CreateDepartmentCommand command) {
        Department entity = toDepartment(command);
        entity.setId(dao.insert(entity));
        return entity.getId();
    }

    @Override
    @AuditLog(type = "Department", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "更新部门")
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(ChangeDepartmentInfoCommand command) {
        Department entity = toDepartment(command);
        dao.update(entity);
    }

    @AuditLog(
            type = "Department",
            id = "#command.id.value()",
            action = AuditAction.DELETE,
            summary = "删除部门",
            recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public int remove(DeleteDepartmentCommand command) {
        Department bean = this.get(command.getId());
        if (bean == null) {
            return 0;
        }

        int count = dao.deleteById(bean.getId());

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(MoveDepartmentCommand command) {
        dao.moveTreeNode(
                DepartmentIdCodec.toValue(command.getFromId()),
                DepartmentIdCodec.toValue(command.getToId()),
                command.getMoveType());
    }

    @Override
    public boolean existsChildRelation(DepartmentQuery query) {
        return query != null
                && query.getChildId() != null
                && query.getAncestorId() != null
                && dao.isChildOf(
                        DepartmentIdCodec.toValue(query.getChildId()),
                        DepartmentIdCodec.toValue(query.getAncestorId()));
    }

    private PageQuery normalizePage(PageQuery page) {
        PageQuery normalizedPage = page == null ? new PageQuery() : page;
        if (normalizedPage.getPageNo() < PageRules.firstPageIndex()) {
            normalizedPage.setPageNo(PageRules.firstPageIndex());
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(PageRules.defaultPageSize());
        }
        return normalizedPage;
    }

    private Department toDepartment(CreateDepartmentCommand command) {
        Department department = new Department();
        department.setId(command.getId());
        department.setParentId(command.getParentId());
        department.setName(command.getName());
        department.setShortName(command.getShortName());
        department.setPriority(command.getPriority());
        department.setRemarks(command.getRemarks());
        return department;
    }

    private Department toDepartment(ChangeDepartmentInfoCommand command) {
        Department department = new Department();
        department.setId(command.getId());
        department.setParentId(command.getParentId());
        department.setName(command.getName());
        department.setShortName(command.getShortName());
        department.setPriority(command.getPriority());
        department.setRemarks(command.getRemarks());
        return department;
    }
}
