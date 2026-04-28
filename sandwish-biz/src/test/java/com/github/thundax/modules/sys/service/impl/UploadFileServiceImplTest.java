package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.sys.dao.UploadFileDao;
import com.github.thundax.modules.sys.entity.UploadFile;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class UploadFileServiceImplTest {

    @Test
    public void shouldIgnoreBlankId() {
        RecordingUploadFileDao dao = new RecordingUploadFileDao();
        UploadFileServiceImpl service = new UploadFileServiceImpl(dao);

        assertEquals(null, service.get(""));
        assertEquals(0, dao.getCalls);
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingUploadFileDao dao = new RecordingUploadFileDao();
        Page<UploadFile> page = new Page<>();
        page.setPageNo(0);
        page.setPageSize(0);
        UploadFileServiceImpl service = new UploadFileServiceImpl(dao);

        service.findPage(new UploadFile(), page);

        assertEquals(Page.FIRST_PAGE_INDEX, dao.pageNo);
        assertEquals(Page.DEFAULT_PAGE_SIZE, dao.pageSize);
        assertEquals(1L, page.getCount());
    }

    @Test
    public void shouldPrepareFileBeforeInsert() {
        RecordingUploadFileDao dao = new RecordingUploadFileDao();
        UploadFile file = new UploadFile();
        UploadFileServiceImpl service = new UploadFileServiceImpl(dao);

        service.add(file);

        assertNotNull(file.getId());
        assertNotNull(file.getCreateDate());
        assertSame(file, dao.inserted);
    }

    @Test
    public void shouldLoadContentById() {
        RecordingUploadFileDao dao = new RecordingUploadFileDao();
        UploadFile expected = new UploadFile("file-1");
        dao.contentResult = expected;
        UploadFileServiceImpl service = new UploadFileServiceImpl(dao);

        UploadFile actual = service.getContent(new UploadFile("file-1"));

        assertSame(expected, actual);
        assertEquals("file-1", dao.contentId);
    }

    @Test
    public void shouldExpandFileIdArray() {
        RecordingUploadFileDao dao = new RecordingUploadFileDao();
        UploadFileServiceImpl service = new UploadFileServiceImpl(dao);

        service.findByFileIds(new String[] {"f1", "f2"});

        assertEquals(Arrays.asList("f1", "f2"), dao.fileIds);
    }

    private static class RecordingUploadFileDao implements UploadFileDao {

        private int getCalls;
        private int pageNo;
        private int pageSize;
        private UploadFile inserted;
        private UploadFile contentResult;
        private String contentId;
        private List<String> fileIds;

        @Override
        public UploadFile get(String id) {
            this.getCalls++;
            return null;
        }

        @Override
        public List<UploadFile> getMany(List<String> idList) {
            return null;
        }

        @Override
        public List<UploadFile> findList() {
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<UploadFile> findPage(
                int pageNo, int pageSize) {
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<UploadFile> dataPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
            dataPage.setTotal(1);
            return dataPage;
        }

        @Override
        public int insert(UploadFile uploadFile) {
            this.inserted = uploadFile;
            return 1;
        }

        @Override
        public int update(UploadFile uploadFile) {
            return 1;
        }

        @Override
        public int updatePriority(UploadFile uploadFile) {
            return 1;
        }

        @Override
        public int updateDelFlag(UploadFile uploadFile) {
            return 1;
        }

        @Override
        public int delete(String id) {
            return 1;
        }

        @Override
        public UploadFile getContent(String id) {
            this.contentId = id;
            return contentResult;
        }

        @Override
        public List<UploadFile> findByFileIds(List<String> fileIds) {
            this.fileIds = fileIds;
            return null;
        }
    }
}
