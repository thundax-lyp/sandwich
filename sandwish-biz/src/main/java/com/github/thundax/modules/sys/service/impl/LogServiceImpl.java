package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.LogDao;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.service.LogService;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LogServiceImpl implements LogService {

    private static final int BATCH_INSERT_SIZE = 50;
    private final LogDao dao;
    private final SignService signService;

    public LogServiceImpl(LogDao dao, SignService signService) {
        this.dao = dao;
        this.signService = signService;
    }

    @Override
    public Log get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return dao.get(id);
    }

    @Override
    public List<Log> findList(Log log) {
        Log.Query query = log == null ? null : log.getQuery();
        return dao.findList(
                query == null ? null : query.getType(),
                query == null ? null : query.getRemoteAddr(),
                query == null ? null : query.getUserLoginName(),
                query == null ? null : query.getUserName(),
                query == null ? null : query.getTitle(),
                query == null ? null : query.getRequestUri(),
                query == null ? null : query.getBeginDate(),
                query == null ? null : query.getEndDate());
    }

    @Override
    public Page<Log> findPage(Log log, Page<Log> page) {
        Page<Log> normalizedPage = normalizePage(page);
        Log.Query query = log == null ? null : log.getQuery();
        IPage<Log> dataPage = dao.findPage(
                query == null ? null : query.getType(),
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
    public void save(Log log) {
        log.preInsert();
        dao.insert(log);

        if (log.isSignable()) {
            signService.sign(log.getSignName(), log.getSignId(), log.getSignBody());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Log log) {
        if (log == null) {
            return 0;
        }
        return dao.delete(log.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertList(List<Log> list) {
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
            subList.forEach(Log::preInsert);
            count += dao.insertList(subList);
        }

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Log log) {
        Log.Query query = log == null ? null : log.getQuery();
        return dao.batchDelete(
                query == null ? null : query.getType(),
                query == null ? null : query.getRemoteAddr(),
                query == null ? null : query.getTitle(),
                query == null ? null : query.getRequestUri(),
                query == null ? null : query.getBeginDate(),
                query == null ? null : query.getEndDate());
    }

    private Page<Log> normalizePage(Page<Log> page) {
        Page<Log> normalizedPage = page == null ? new Page<>() : page;
        if (normalizedPage.getPageNo() < Page.FIRST_PAGE_INDEX) {
            normalizedPage.setPageNo(Page.FIRST_PAGE_INDEX);
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(Page.DEFAULT_PAGE_SIZE);
        }
        return normalizedPage;
    }
}
