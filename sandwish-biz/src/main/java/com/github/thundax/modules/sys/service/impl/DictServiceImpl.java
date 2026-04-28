package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.DictService;
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
        return new Dict(id);
    }

    @Override
    public Dict get(Dict entity) {
        return entity == null ? null : get(entity.getId());
    }

    @Override
    public Dict get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return dao.get(id);
    }

    @Override
    public List<Dict> getMany(List<String> ids) {
        return dao.getMany(ids);
    }

    @Override
    public List<String> findTypeList() {
        return dao.findTypeList();
    }

    public List<String> findLabelList(String type) {
        List<String> result = new ArrayList<String>();
        Dict query = new Dict();
        Dict.Query queryCondition = new Dict.Query();
        queryCondition.setType(type);
        query.setQuery(queryCondition);
        List<Dict> list = findList(query);
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
    public List<Dict> findList(Dict dict) {
        Dict.Query query = dict == null ? null : dict.getQuery();
        return dao.findList(
                query == null ? null : query.getType(),
                query == null ? null : query.getLabel(),
                query == null ? null : query.getRemarks());
    }

    @Override
    public Dict findOne(Dict dict) {
        List<Dict> dicts = findList(dict);
        return dicts == null || dicts.isEmpty() ? null : dicts.get(0);
    }

    @Override
    public Page<Dict> findPage(Dict dict, Page<Dict> page) {
        Page<Dict> normalizedPage = normalizePage(page);
        Dict.Query query = dict == null ? null : dict.getQuery();
        IPage<Dict> dataPage = dao.findPage(
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
        List<Dict> dicts = findList(dict);
        return dicts == null ? 0 : dicts.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Dict dict) {
        dict.preInsert();
        dict.setId(dao.insert(dict));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dict dict) {
        dict.preUpdate();
        dao.update(dict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Dict dict) {
        return dict == null ? 0 : dao.delete(dict.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(List<Dict> list) {
        return batchOperate(list, this::delete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(Dict dict) {
        dict.preUpdate();
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

    private int batchOperate(Collection<Dict> collection, Function<Dict, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (Dict dict : collection) {
                count += operator.apply(dict);
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
