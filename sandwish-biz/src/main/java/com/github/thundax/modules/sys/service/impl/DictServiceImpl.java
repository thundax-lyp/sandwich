package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.DictService;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DictServiceImpl implements DictService {

    private final DictDao dao;

    public DictServiceImpl(DictDao dao) {
        this.dao = dao;
    }

    public Dict getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<String> listTypes() {
        return dao.listTypes();
    }

    public List<String> listLabels(String type) {
        List<String> result = new ArrayList<String>();
        DictQuery query = new DictQuery();
        query.setType(type);
        List<Dict> list = list(query);
        String s = "";
        for (Dict item : list) {
            s = item.getLabel();
            if (StringUtils.isNotEmpty(s)) {
                result.add(s);
            }
        }
        return result;
    }

    public List<Dict> list(DictQuery query) {
        return dao.list(
                query == null ? null : query.getType(),
                query == null ? null : query.getLabel(),
                query == null ? null : query.getRemarks());
    }

    public PageResult<Dict> page(DictQuery query, PageQuery page) {
        PageQuery normalizedPage = normalizePage(page);
        IPage<Dict> dataPage = dao.page(
                query == null ? null : query.getType(),
                query == null ? null : query.getLabel(),
                query == null ? null : query.getRemarks(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(),
                (int) dataPage.getSize(),
                dataPage.getTotal(),
                dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId add(Dict dict) {
        dict.setId(dao.insert(dict));
        return dict.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dict dict) {
        dao.update(dict);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        return id == null ? 0 : dao.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
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
}
