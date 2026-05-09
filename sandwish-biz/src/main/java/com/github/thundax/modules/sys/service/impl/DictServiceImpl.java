package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.DictService;
import com.github.thundax.modules.sys.service.command.ChangeDictInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateDictCommand;
import com.github.thundax.modules.sys.service.command.DeleteDictCommand;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.ArrayList;
import java.util.List;
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

    public Dict get(DictQuery query) {
        if (query == null || query.getId() == null) {
            return null;
        }
        return dao.getById(query.getId());
    }

    @Override
    public List<String> listTypes(DictQuery query) {
        return dao.listTypes();
    }

    public List<String> listLabels(DictQuery query) {
        List<String> result = new ArrayList<String>();
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
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId create(CreateDictCommand command) {
        Dict dict = toEntity(command);
        dict.setId(dao.insert(dict));
        return dict.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(ChangeDictInfoCommand command) {
        dao.update(toEntity(command));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(DeleteDictCommand command) {
        if (command != null && command.getId() != null) {
            dao.deleteById(command.getId());
        }
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

    private Dict toEntity(CreateDictCommand command) {
        Dict dict = new Dict();
        if (command == null) {
            return dict;
        }
        dict.setType(command.getType());
        dict.setLabel(command.getLabel());
        dict.setValue(command.getValue());
        if (command.getPriority() != null) {
            dict.setPriority(command.getPriority());
        }
        dict.setRemarks(command.getRemarks());
        return dict;
    }

    private Dict toEntity(ChangeDictInfoCommand command) {
        Dict dict = new Dict();
        if (command == null) {
            return dict;
        }
        dict.setId(command.getId());
        dict.setType(command.getType());
        dict.setLabel(command.getLabel());
        dict.setValue(command.getValue());
        if (command.getPriority() != null) {
            dict.setPriority(command.getPriority());
        }
        dict.setRemarks(command.getRemarks());
        return dict;
    }
}
