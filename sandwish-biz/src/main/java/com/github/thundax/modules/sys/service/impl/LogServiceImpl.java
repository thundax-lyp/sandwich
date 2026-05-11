package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.exception.BizExceptionBoundary;
import com.github.thundax.modules.sys.dao.LogDao;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.enums.LogType;
import com.github.thundax.modules.sys.entity.valueobject.LogId;
import com.github.thundax.modules.sys.service.LogService;
import com.github.thundax.modules.sys.service.command.CreateLogCommand;
import com.github.thundax.modules.sys.service.query.LogQuery;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LogServiceImpl implements LogService {

    private final LogDao dao;

    public LogServiceImpl(LogDao dao) {
        this.dao = dao;
    }

    @Override
    @BizExceptionBoundary
    public Log get(LogId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    @BizExceptionBoundary
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
    @BizExceptionBoundary
    public PageResult<Log> page(LogQuery query, PageQuery page) {
        PageQuery normalizedPage = normalizePage(page);
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
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @BizExceptionBoundary
    @Transactional(rollbackFor = Exception.class)
    public LogId create(CreateLogCommand command) {
        Log log = toLog(command);
        log.setId(dao.insert(log));
        return log.getId();
    }

    @Override
    @BizExceptionBoundary
    @Transactional(rollbackFor = Exception.class)
    public int deleteByCondition(LogQuery query) {
        return dao.batchDelete(
                query == null ? null : typeValue(query.getType()),
                query == null ? null : query.getRemoteAddr(),
                query == null ? null : query.getTitle(),
                query == null ? null : query.getRequestUri(),
                query == null ? null : query.getBeginDate(),
                query == null ? null : query.getEndDate());
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

    private String typeValue(LogType type) {
        return type == null ? null : type.value();
    }

    private Log toLog(CreateLogCommand command) {
        Log log = new Log();
        log.setId(command.getId());
        log.setUserId(command.getUserId());
        log.setType(command.getType());
        log.setLogDate(command.getLogDate());
        log.setTitle(command.getTitle());
        log.setRemoteAddr(command.getRemoteAddr());
        log.setUserAgent(command.getUserAgent());
        log.setMethod(command.getMethod());
        log.setRequestUri(command.getRequestUri());
        log.setRequestParams(command.getRequestParams());
        log.setRemarks(command.getRemarks());
        return log;
    }
}
