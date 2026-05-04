package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
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

        assertEquals(null, service.getById((EntityId) null));
        assertEquals(0, dao.getCalls);
    }

    @Test
    public void shouldPrepareFileBeforeInsert() {
        RecordingUploadFileDao dao = new RecordingUploadFileDao();
        UploadFile file = new UploadFile();
        UploadFileServiceImpl service = new UploadFileServiceImpl(dao);

        service.add(file);

        assertNotNull(file.getId());
        assertEquals(null, file.getCreateDate());
        assertSame(file, dao.inserted);
    }

    @Test
    public void shouldLoadContentById() {
        RecordingUploadFileDao dao = new RecordingUploadFileDao();
        UploadFile expected = uploadFile("file-1");
        dao.contentResult = expected;
        UploadFileServiceImpl service = new UploadFileServiceImpl(dao);

        UploadFile actual = service.getContent(uploadFile("file-1"));

        assertSame(expected, actual);
        assertEquals("file-1", dao.contentId);
    }

    @Test
    public void shouldExpandFileIdArray() {
        RecordingUploadFileDao dao = new RecordingUploadFileDao();
        UploadFileServiceImpl service = new UploadFileServiceImpl(dao);

        service.batchGetByFileIds(new String[] {"f1", "f2"});

        assertEquals(Arrays.asList("f1", "f2"), dao.fileIds);
    }

    private static UploadFile uploadFile(String id) {
        UploadFile uploadFile = new UploadFile();
        uploadFile.setId(EntityIdCodec.toDomain(id));
        return uploadFile;
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
        public UploadFile getById(EntityId id) {
            this.getCalls++;
            return null;
        }

        @Override
        public List<UploadFile> listByIds(List<String> idList) {
            return null;
        }

        @Override
        public List<UploadFile> list() {
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<UploadFile> page(int pageNo, int pageSize) {
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<UploadFile> dataPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
            dataPage.setTotal(1);
            return dataPage;
        }

        @Override
        public String insert(UploadFile uploadFile) {
            this.inserted = uploadFile;
            return "generated-upload-file-id";
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
        public int deleteById(EntityId id) {
            return 1;
        }

        @Override
        public UploadFile getContentById(EntityId id) {
            this.contentId = id.value();
            return contentResult;
        }

        @Override
        public List<UploadFile> batchGetByFileIds(List<String> fileIds) {
            this.fileIds = fileIds;
            return null;
        }
    }
}
