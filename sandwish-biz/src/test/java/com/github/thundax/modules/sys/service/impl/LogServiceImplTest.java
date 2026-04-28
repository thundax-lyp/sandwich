package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.LogDao;
import com.github.thundax.modules.sys.entity.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class LogServiceImplTest {

    @Test
    public void shouldIgnoreBlankId() {
        RecordingLogDao dao = new RecordingLogDao();
        LogServiceImpl service = new LogServiceImpl(dao, new RecordingSignService());

        assertEquals(null, service.get(""));
        assertEquals(0, dao.getCalls);
    }

    @Test
    public void shouldGetLogById() {
        RecordingLogDao dao = new RecordingLogDao();
        Log expected = new Log("log-1");
        dao.getResult = expected;
        LogServiceImpl service = new LogServiceImpl(dao, new RecordingSignService());

        assertSame(expected, service.get("log-1"));

        assertEquals("log-1", dao.id);
    }

    @Test
    public void shouldExpandFindPageQuery() {
        RecordingLogDao dao = new RecordingLogDao();
        Log log = new Log();
        Log.Query query = new Log.Query();
        Date begin = new Date(1000L);
        Date end = new Date(2000L);
        query.setType(Log.TYPE_ACCESS);
        query.setRemoteAddr("127.0.0.1");
        query.setUserLoginName("admin");
        query.setUserName("系统管理员");
        query.setTitle("登录");
        query.setRequestUri("/login");
        query.setBeginDate(begin);
        query.setEndDate(end);
        log.setQuery(query);
        Page<Log> page = new Page<>(2, 20, 100);
        LogServiceImpl service = new LogServiceImpl(dao, new RecordingSignService());

        service.findPage(log, page);

        assertEquals(Log.TYPE_ACCESS, dao.type);
        assertEquals("127.0.0.1", dao.remoteAddr);
        assertEquals("admin", dao.userLoginName);
        assertEquals("系统管理员", dao.userName);
        assertEquals("登录", dao.title);
        assertEquals("/login", dao.requestUri);
        assertEquals(begin, dao.beginDate);
        assertEquals(end, dao.endDate);
        assertEquals(2, dao.pageNo);
        assertEquals(20, dao.pageSize);
        assertEquals(1L, page.getCount());
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingLogDao dao = new RecordingLogDao();
        Page<Log> page = new Page<>();
        page.setPageNo(0);
        page.setPageSize(0);
        LogServiceImpl service = new LogServiceImpl(dao, new RecordingSignService());

        service.findPage(new Log(), page);

        assertEquals(Page.FIRST_PAGE_INDEX, dao.pageNo);
        assertEquals(Page.DEFAULT_PAGE_SIZE, dao.pageSize);
    }

    @Test
    public void shouldPrepareAndSignLogBeforeSave() {
        RecordingLogDao dao = new RecordingLogDao();
        RecordingSignService signService = new RecordingSignService();
        Log log = new Log();
        log.setSignable(true);
        log.setType(Log.TYPE_EXCEPTION);
        LogServiceImpl service = new LogServiceImpl(dao, signService);

        service.add(log);

        assertNotNull(log.getId());
        assertNotNull(log.getCreateDate());
        assertSame(log, dao.inserted);
        assertEquals(Log.BEAN_NAME, signService.businessType);
        assertEquals(log.getSignId(), signService.businessId);
        assertEquals(log.getSignBody(), signService.body);
    }

    @Test
    public void shouldBatchInsertAndPrepareEveryLog() {
        RecordingLogDao dao = new RecordingLogDao();
        LogServiceImpl service = new LogServiceImpl(dao, new RecordingSignService());
        List<Log> logs = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            logs.add(new Log());
        }

        int count = service.insertList(logs);

        assertEquals(51, count);
        assertEquals(2, dao.insertListCalls);
        assertEquals(50, dao.firstBatchSize);
        assertEquals(1, dao.secondBatchSize);
        for (Log log : logs) {
            assertNotNull(log.getId());
            assertNotNull(log.getCreateDate());
        }
    }

    @Test
    public void shouldBatchDeleteWithoutUserFilters() {
        RecordingLogDao dao = new RecordingLogDao();
        Log log = new Log();
        Log.Query query = new Log.Query();
        Date begin = new Date(1000L);
        Date end = new Date(2000L);
        query.setType(Log.TYPE_EXCEPTION);
        query.setRemoteAddr("10.0.0.1");
        query.setUserLoginName("ignored");
        query.setUserName("ignored");
        query.setTitle("错误");
        query.setRequestUri("/api");
        query.setBeginDate(begin);
        query.setEndDate(end);
        log.setQuery(query);
        LogServiceImpl service = new LogServiceImpl(dao, new RecordingSignService());

        service.batchDelete(log);

        assertEquals(Log.TYPE_EXCEPTION, dao.type);
        assertEquals("10.0.0.1", dao.remoteAddr);
        assertEquals(null, dao.userLoginName);
        assertEquals(null, dao.userName);
        assertEquals("错误", dao.title);
        assertEquals("/api", dao.requestUri);
        assertEquals(begin, dao.beginDate);
        assertEquals(end, dao.endDate);
    }

    private static class RecordingLogDao implements LogDao {

        private Log getResult;
        private String id;
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
        private int insertListCalls;
        private int firstBatchSize;
        private int secondBatchSize;

        @Override
        public Log get(String id) {
            this.getCalls++;
            this.id = id;
            return getResult;
        }

        @Override
        public List<Log> getMany(List<String> idList) {
            return null;
        }

        @Override
        public List<Log> findList(
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
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Log> findPage(
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
        public String insert(Log log) {
            this.inserted = log;
            return "generated-log-id";
        }

        @Override
        public int update(Log log) {
            return 1;
        }

        @Override
        public int delete(String id) {
            return 1;
        }

        @Override
        public List<String> insertList(List<Log> list) {
            this.insertListCalls++;
            if (insertListCalls == 1) {
                firstBatchSize = list.size();
            } else {
                secondBatchSize = list.size();
            }
            List<String> idList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                idList.add("generated-log-id-" + insertListCalls + "-" + i);
            }
            return idList;
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

    private static class RecordingSignService implements SignService {

        private String businessType;
        private String businessId;
        private String body;

        @Override
        public Boolean sign(String businessType, String businessId, String body) {
            this.businessType = businessType;
            this.businessId = businessId;
            this.body = body;
            return true;
        }

        @Override
        public Boolean verifySign(String businessType, String businessId, String body) {
            return true;
        }

        @Override
        public void deleteSign(String businessType, String businessId) {}
    }
}
