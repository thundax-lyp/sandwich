package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.sys.dao.OfficeDao;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.service.OfficeService;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
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
        return new Office(id);
    }

    @Override
    public Office get(Office entity) {
        return entity == null ? null : get(entity.getId());
    }

    @Override
    public Office get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return dao.get(id);
    }

    @Override
    public List<Office> getMany(List<String> ids) {
        return dao.getMany(ids);
    }

    @Override
    public List<Office> findList(Office office) {
        Office.Query query = office == null ? null : office.getQuery();
        return dao.findList(
                query == null ? null : query.getParentId(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks());
    }

    @Override
    public Office findOne(Office office) {
        List<Office> offices = findList(office);
        return offices == null || offices.isEmpty() ? null : offices.get(0);
    }

    @Override
    public Page<Office> findPage(Office office, Page<Office> page) {
        Page<Office> normalizedPage = normalizePage(page);
        Office.Query query = office == null ? null : office.getQuery();
        IPage<Office> dataPage = dao.findPage(
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
        return findList(office).size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Office entity) {
        entity.setId(dao.insert(entity));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Office entity) {
        dao.update(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Office entity) {
        Office bean = this.get(entity.getId());
        if (bean == null) {
            return 0;
        }

        int count = dao.delete(bean.getId());

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(List<Office> list) {
        return batchOperate(list, this::delete);
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
        dao.moveTreeNode(from.getId(), to.getId(), moveType);
    }

    @Override
    public boolean isChildOf(Office child, Office parent) {
        return child != null && parent != null && dao.isChildOf(child.getId(), parent.getId());
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
