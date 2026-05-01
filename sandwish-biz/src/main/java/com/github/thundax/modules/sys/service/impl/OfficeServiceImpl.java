package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.sys.dao.OfficeDao;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.service.OfficeService;
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

    @Override
    public Class<Office> getElementType() {
        return Office.class;
    }

    @Override
    public Office newEntity(String id) {
        Office office = new Office();
        office.setId(EntityIdCodec.toDomain(id));
        return office;
    }

    @Override
    public Office getById(Office entity) {
        return entity == null ? null : getById(entity.getId());
    }

    @Override
    public Office getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Office> batchGetByIds(List<String> ids) {
        return dao.batchGetByIds(ids);
    }

    @Override
    public List<Office> list(Office office) {
        Office.Query query = office == null ? null : office.getQuery();
        return dao.list(
                query == null ? null : query.getParentId(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks());
    }

    @Override
    public Office getOne(Office office) {
        List<Office> offices = list(office);
        return offices == null || offices.isEmpty() ? null : offices.get(0);
    }

    @Override
    public Page<Office> page(Office office, Page<Office> page) {
        Page<Office> normalizedPage = normalizePage(page);
        Office.Query query = office == null ? null : office.getQuery();
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

    @Override
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Office entity) {
        Office bean = this.getById(entity.getId());
        if (bean == null) {
            return 0;
        }

        int count = dao.deleteById(bean.getId());

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<Office> list) {
        return batchOperate(list, this::deleteById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(Office office) {
        return dao.updatePriority(office);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<Office> list) {
        return batchOperate(list, this::updatePriority);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(Office from, Office to, MoveTreeNodeType moveType) {
        dao.moveTreeNode(EntityIdCodec.toValue(from.getId()), EntityIdCodec.toValue(to.getId()), moveType);
    }

    @Override
    public boolean isChildOf(Office child, Office parent) {
        return child != null
                && parent != null
                && dao.isChildOf(EntityIdCodec.toValue(child.getId()), EntityIdCodec.toValue(parent.getId()));
    }

    private int batchOperate(Collection<Office> collection, Function<Office, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (Office office : collection) {
                count += operator.apply(office);
            }
        }
        return count;
    }

    private Page<Office> normalizePage(Page<Office> page) {
        Page<Office> normalizedPage = page == null ? new Page<>() : page;
        if (normalizedPage.getPageNo() < Page.FIRST_PAGE_INDEX) {
            normalizedPage.setPageNo(Page.FIRST_PAGE_INDEX);
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(Page.DEFAULT_PAGE_SIZE);
        }
        return normalizedPage;
    }
}
