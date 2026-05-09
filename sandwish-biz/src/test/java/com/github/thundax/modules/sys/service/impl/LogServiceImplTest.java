package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.sys.dao.LogDao;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.enums.LogType;
import com.github.thundax.modules.sys.service.command.CreateLogCommand;
import com.github.thundax.modules.sys.service.query.LogQuery;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class LogServiceImplTest {

    @Test
    public void shouldIgnoreBlankId() {
        RecordingLogDao dao = new RecordingLogDao();
        LogServiceImpl service = new LogServiceImpl(dao);

        assertEquals(null, service.get((LogQuery) null));
        assertEquals(0, dao.getCalls);
    }

    @Test
    public void shouldGetLogById() {
        RecordingLogDao dao = new RecordingLogDao();
        Log expected = log(7001L);
        dao.getResult = expected;
        LogServiceImpl service = new LogServiceImpl(dao);
        LogQuery query = new LogQuery();
        query.setId(EntityId.of(7001L));

        assertSame(expected, service.get(query));

        assertEquals(Long.valueOf(7001L), dao.id);
    }

    @Test
    public void shouldExpandFindPageQuery() {
        RecordingLogDao dao = new RecordingLogDao();
        LogQuery query = new LogQuery();
        Date begin = new Date(1000L);
        Date end = new Date(2000L);
        query.setType(LogType.ACCESS);
        query.setRemoteAddr("127.0.0.1");
        query.setUserLoginName("admin");
        query.setUserName("系统管理员");
        query.setTitle("登录");
        query.setRequestUri("/login");
        query.setBeginDate(begin);
        query.setEndDate(end);
        PageQuery page = new PageQuery(2, 20);
        LogServiceImpl service = new LogServiceImpl(dao);

        PageResult<Log> result = service.page(query, page);

        assertEquals("ACCESS", dao.type);
        assertEquals("127.0.0.1", dao.remoteAddr);
        assertEquals("admin", dao.userLoginName);
        assertEquals("系统管理员", dao.userName);
        assertEquals("登录", dao.title);
        assertEquals("/login", dao.requestUri);
        assertEquals(begin, dao.beginDate);
        assertEquals(end, dao.endDate);
        assertEquals(2, dao.pageNo);
        assertEquals(20, dao.pageSize);
        assertEquals(1L, result.getTotalCount());
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingLogDao dao = new RecordingLogDao();
        PageQuery page = new PageQuery();
        page.setPageNo(0);
        page.setPageSize(0);
        LogServiceImpl service = new LogServiceImpl(dao);

        service.page((LogQuery) null, page);

        assertEquals(PageRules.firstPageIndex(), dao.pageNo);
        assertEquals(PageRules.defaultPageSize(), dao.pageSize);
    }

    @Test
    public void shouldPrepareLogBeforeSave() {
        RecordingLogDao dao = new RecordingLogDao();
        Log log = new Log();
        log.setType(LogType.EXCEPTION);
        LogServiceImpl service = new LogServiceImpl(dao);

        EntityId id = service.create(createCommand(log));

        assertNotNull(id);
        assertNotNull(dao.inserted);
    }

    @Test
    public void shouldBatchDeleteWithoutUserFilters() {
        RecordingLogDao dao = new RecordingLogDao();
        LogQuery query = new LogQuery();
        Date begin = new Date(1000L);
        Date end = new Date(2000L);
        query.setType(LogType.EXCEPTION);
        query.setRemoteAddr("10.0.0.1");
        query.setUserLoginName("ignored");
        query.setUserName("ignored");
        query.setTitle("错误");
        query.setRequestUri("/api");
        query.setBeginDate(begin);
        query.setEndDate(end);
        LogServiceImpl service = new LogServiceImpl(dao);

        service.deleteByCondition(query);

        assertEquals("EXCEPTION", dao.type);
        assertEquals("10.0.0.1", dao.remoteAddr);
        assertEquals(null, dao.userLoginName);
        assertEquals(null, dao.userName);
        assertEquals("错误", dao.title);
        assertEquals("/api", dao.requestUri);
        assertEquals(begin, dao.beginDate);
        assertEquals(end, dao.endDate);
    }

    private static Log log(Long id) {
        Log log = new Log();
        log.setId(EntityIdCodec.toDomain(id));
        return log;
    }

    private static CreateLogCommand createCommand(Log log) {
        return new CreateLogCommand(
                log.getId(),
                log.getUserId(),
                log.getType(),
                log.getLogDate(),
                log.getTitle(),
                log.getRemoteAddr(),
                log.getUserAgent(),
                log.getMethod(),
                log.getRequestUri(),
                log.getRequestParams(),
                log.getRemarks());
    }

    private static class RecordingLogDao implements LogDao {

        private Log getResult;
        private Long id;
        private int getCalls;
        private String type;
        private String remoteAddr;
        private String userLoginName;
        private String userName;
        private String title;
        private String requestUri;
        private Date beginDate;
        private Date endDate;
        private int pageNo;
        private int pageSize;
        private Log inserted;

        @Override
        public Log getById(EntityId id) {
            this.getCalls++;
            this.id = id.value();
            return getResult;
        }

        @Override
        public List<Log> listByIds(List<String> idList) {
            return null;
        }

        @Override
        public List<Log> list(
                String type,
                String remoteAddr,
                String userLoginName,
                String userName,
                String title,
                String requestUri,
                Date beginDate,
                Date endDate) {
            recordFilters(type, remoteAddr, userLoginName, userName, title, requestUri, beginDate, endDate);
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Log> page(
                String type,
                String remoteAddr,
                String userLoginName,
                String userName,
                String title,
                String requestUri,
                Date beginDate,
                Date endDate,
                int pageNo,
                int pageSize) {
            recordFilters(type, remoteAddr, userLoginName, userName, title, requestUri, beginDate, endDate);
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Log> dataPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
            dataPage.setTotal(1);
            return dataPage;
        }

        @Override
        public EntityId insert(Log log) {
            this.inserted = log;
            return EntityId.of(9701L);
        }

        @Override
        public int update(Log log) {
            return 1;
        }

        @Override
        public int deleteById(EntityId id) {
            return 1;
        }

        @Override
        public List<EntityId> batchInsert(List<Log> list) {
            return java.util.Collections.emptyList();
        }

        @Override
        public int batchDelete(
                String type, String remoteAddr, String title, String requestUri, Date beginDate, Date endDate) {
            recordFilters(type, remoteAddr, null, null, title, requestUri, beginDate, endDate);
            return 1;
        }

        private void recordFilters(
                String type,
                String remoteAddr,
                String userLoginName,
                String userName,
                String title,
                String requestUri,
                Date beginDate,
                Date endDate) {
            this.type = type;
            this.remoteAddr = remoteAddr;
            this.userLoginName = userLoginName;
            this.userName = userName;
            this.title = title;
            this.requestUri = requestUri;
            this.beginDate = beginDate;
            this.endDate = endDate;
        }
    }
}
