package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
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

    @Override
    public Class<Dict> getElementType() {
        return Dict.class;
    }

    @Override
    public Dict newEntity(String id) {
        Dict dict = new Dict();
        dict.setId(EntityIdCodec.toDomain(id));
        return dict;
    }

    @Override
    public Dict getById(Dict entity) {
        return entity == null ? null : getById(entity.getId());
    }

    @Override
    public Dict getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Dict> batchGetByIds(List<EntityId> ids) {
        return dao.batchGetByIds(EntityIdCodec.toValues(ids));
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

    @Override
    public List<Dict> list(Dict dict) {
        return list((DictQuery) null);
    }

    @Override
    public List<Dict> list(DictQuery query) {
        return dao.list(
                query == null ? null : query.getType(),
                query == null ? null : query.getLabel(),
                query == null ? null : query.getRemarks());
    }

    @Override
    public Dict getOne(Dict dict) {
        List<Dict> dicts = list(dict);
        return dicts == null || dicts.isEmpty() ? null : dicts.get(0);
    }

    @Override
    public Page<Dict> page(Dict dict, Page<Dict> page) {
        return page((DictQuery) null, page);
    }

    @Override
    public Page<Dict> page(DictQuery query, Page<Dict> page) {
        Page<Dict> normalizedPage = normalizePage(page);
        IPage<Dict> dataPage = dao.page(
                query == null ? null : query.getType(),
                query == null ? null : query.getLabel(),
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
    public long count(Dict dict) {
        List<Dict> dicts = list(dict);
        return dicts == null ? 0 : dicts.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Dict dict) {
        dict.setId(EntityIdCodec.toDomain(dao.insert(dict)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dict dict) {
        dao.update(dict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        return id == null ? 0 : dao.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(Dict dict) {
        return dao.updatePriority(dict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<Dict> list) {
        return batchOperate(list, this::updatePriority);
    }

    @Override
    public String getDictionaryRevision() {
        return dao.getDictionaryRevision();
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

    private Page<Dict> normalizePage(Page<Dict> page) {
        Page<Dict> normalizedPage = page == null ? new Page<>() : page;
        if (normalizedPage.getPageNo() < Page.FIRST_PAGE_INDEX) {
            normalizedPage.setPageNo(Page.FIRST_PAGE_INDEX);
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(Page.DEFAULT_PAGE_SIZE);
        }
        return normalizedPage;
    }
}
