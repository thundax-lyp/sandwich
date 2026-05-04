package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.dao.OfficeDao;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.service.OfficeService;
import com.github.thundax.modules.sys.service.query.OfficeQuery;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OfficeServiceImpl implements OfficeService {

    private final OfficeDao dao;

    public OfficeServiceImpl(OfficeDao dao) {
        this.dao = dao;
    }

    public Class<Office> getElementType() {
        return Office.class;
    }

    public Office newEntity(String id) {
        Office office = new Office();
        office.setId(EntityIdCodec.toDomain(id));
        return office;
    }

    public Office getById(Office entity) {
        return entity == null ? null : getById(entity.getId());
    }

    public Office getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<Office> batchGetByIds(List<EntityId> ids) {
        return dao.batchGetByIds(EntityIdCodec.toValues(ids));
    }

    public List<Office> list(Office office) {
        return list((OfficeQuery) null);
    }

    public List<Office> list(OfficeQuery query) {
        return dao.list(
                query == null ? null : query.getParentId(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks());
    }

    public Office getOne(Office office) {
        List<Office> offices = list(office);
        return offices == null || offices.isEmpty() ? null : offices.get(0);
    }

    public PageDTO<Office> page(Office office, PageDTO<Office> page) {
        return page((OfficeQuery) null, page);
    }

    public PageDTO<Office> page(OfficeQuery query, PageDTO<Office> page) {
        PageDTO<Office> normalizedPage = normalizePage(page);
        IPage<Office> dataPage = dao.page(
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

    public long count(Office office) {
        return list(office).size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Office entity) {
        entity.setId(EntityIdCodec.toDomain(dao.insert(entity)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Office entity) {
        dao.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        Office bean = this.getById(id);
        if (bean == null) {
            return 0;
        }

        int count = dao.deleteById(bean.getId());

        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(Office office) {
        return dao.updatePriority(office);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<Office> list) {
        return batchOperate(list, this::updatePriority);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(Office from, Office to, TreeNodeMoveType moveType) {
        dao.moveTreeNode(EntityIdCodec.toValue(from.getId()), EntityIdCodec.toValue(to.getId()), moveType);
    }

    @Override
    public boolean isChildOf(Office child, Office parent) {
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

    private PageDTO<Office> normalizePage(PageDTO<Office> page) {
        PageDTO<Office> normalizedPage = page == null ? new PageDTO<>() : page;
        if (normalizedPage.getPageNo() < PageRules.firstPageIndex()) {
            normalizedPage.setPageNo(PageRules.firstPageIndex());
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(PageRules.defaultPageSize());
        }
        return normalizedPage;
    }
}
