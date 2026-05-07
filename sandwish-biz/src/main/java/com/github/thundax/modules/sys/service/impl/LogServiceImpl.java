package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.sys.dao.LogDao;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.enums.LogType;
import com.github.thundax.modules.sys.service.LogService;
import com.github.thundax.modules.sys.service.query.LogQuery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LogServiceImpl implements LogService {

    private static final int BATCH_INSERT_SIZE = 50;
    private final LogDao dao;

    public LogServiceImpl(LogDao dao) {
        this.dao = dao;
    }

    @Override
    public Log getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Log> list(LogQuery query) {
        return dao.list(
                query == null ? null : typeValue(query.getType()),
                query == null ? null : query.getRemoteAddr(),
                query == null ? null : query.getUserLoginName(),
                query == null ? null : query.getUserName(),
                query == null ? null : query.getTitle(),
                query == null ? null : query.getRequestUri(),
                query == null ? null : query.getBeginDate(),
                query == null ? null : query.getEndDate());
    }

    @Override
    public PageDTO<Log> page(LogQuery query, PageDTO<Log> page) {
        PageDTO<Log> normalizedPage = normalizePage(page);
        IPage<Log> dataPage = dao.page(
                query == null ? null : typeValue(query.getType()),
                query == null ? null : query.getRemoteAddr(),
                query == null ? null : query.getUserLoginName(),
                query == null ? null : query.getUserName(),
                query == null ? null : query.getTitle(),
                query == null ? null : query.getRequestUri(),
                query == null ? null : query.getBeginDate(),
                query == null ? null : query.getEndDate(),
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
    public EntityId add(Log log) {
        log.setId(dao.insert(log));
        return log.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Log log) {
        dao.update(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        return id == null ? 0 : dao.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<Log> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }

        int count = 0;

        int pageSize = BATCH_INSERT_SIZE;
        int totalPage = (list.size() + pageSize - 1) / pageSize;
        for (int pageNo = 0; pageNo < totalPage; pageNo++) {
            int fromIndex = pageSize * pageNo;
            int toIndex = Math.min(fromIndex + pageSize, list.size());
            List<Log> subList = new ArrayList<>(list.subList(fromIndex, toIndex));
            List<EntityId> idList = dao.batchInsert(subList);
            for (int i = 0; i < idList.size(); i++) {
                subList.get(i).setId(idList.get(i));
            }
            count += idList.size();
        }

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(LogQuery query) {
        return dao.batchDelete(
                query == null ? null : typeValue(query.getType()),
                query == null ? null : query.getRemoteAddr(),
                query == null ? null : query.getTitle(),
                query == null ? null : query.getRequestUri(),
                query == null ? null : query.getBeginDate(),
                query == null ? null : query.getEndDate());
    }

    private PageDTO<Log> normalizePage(PageDTO<Log> page) {
        PageDTO<Log> normalizedPage = page == null ? new PageDTO<>() : page;
        if (normalizedPage.getPageNo() < PageRules.firstPageIndex()) {
            normalizedPage.setPageNo(PageRules.firstPageIndex());
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(PageRules.defaultPageSize());
        }
        return normalizedPage;
    }

    private String typeValue(LogType type) {
        return type == null ? null : type.value();
    }
}
