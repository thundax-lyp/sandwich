package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.dao.DepartmentDao;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentDao dao;

    public DepartmentServiceImpl(DepartmentDao dao) {
        this.dao = dao;
    }

    public Department getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<Department> listAll() {
        return list((DepartmentQuery) null);
    }

    public List<Department> list(DepartmentQuery query) {
        return dao.list(
                query == null ? null : query.getParentId(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks());
    }

    public PageDTO<Department> page(DepartmentQuery query, PageDTO<Department> page) {
        PageDTO<Department> normalizedPage = normalizePage(page);
        IPage<Department> dataPage = dao.page(
                query == null ? null : query.getParentId(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId add(Department entity) {
        entity.setId(EntityIdCodec.toDomain(dao.insert(entity)));
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Department entity) {
        dao.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        Department bean = this.getById(id);
        if (bean == null) {
            return 0;
        }

        int count = dao.deleteById(bean.getId());

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(Department from, Department to, TreeNodeMoveType moveType) {
        dao.moveTreeNode(EntityIdCodec.toValue(from.getId()), EntityIdCodec.toValue(to.getId()), moveType);
    }

    @Override
    public boolean isChildOf(Department child, Department parent) {
        return child != null
                && parent != null
                && dao.isChildOf(EntityIdCodec.toValue(child.getId()), EntityIdCodec.toValue(parent.getId()));
    }

    private <T> int batchOperate(Collection<T> collection, Function<T, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (T entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private PageDTO<Department> normalizePage(PageDTO<Department> page) {
        PageDTO<Department> normalizedPage = page == null ? new PageDTO<>() : page;
        if (normalizedPage.getPageNo() < PageRules.firstPageIndex()) {
            normalizedPage.setPageNo(PageRules.firstPageIndex());
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(PageRules.defaultPageSize());
        }
        return normalizedPage;
    }
}
