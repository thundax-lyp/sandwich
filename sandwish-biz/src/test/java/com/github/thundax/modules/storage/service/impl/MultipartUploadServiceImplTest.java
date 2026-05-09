package com.github.thundax.modules.storage.service.impl;

import static org.junit.Assert.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.exception.BizException;
import com.github.thundax.modules.storage.dao.MultipartUploadDao;
import com.github.thundax.modules.storage.dao.StoredObjectDao;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.*;
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadPartId;
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadSessionId;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.storage.service.command.AbortMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.CompleteMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.InitMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.UploadMultipartPartCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class MultipartUploadServiceImplTest {

    @Test
    public void shouldInitMultipartUploadSession() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        MultipartUploadServiceImpl service = service(dao);
        MultipartUploadSession session = multipartSession();

        MultipartUploadSession saved = service.init(toInitMultipartUploadCommand(session));

        assertNotNull(saved.getId());
        assertNotNull(saved.getUploadId());
        assertEquals(Integer.valueOf(0), saved.getUploadedPartCount());
        assertSame(MultipartUploadStatus.INITIATED, saved.getUploadStatus());
        assertSame(saved, dao.insertedMultipartSession);
    }

    @Test
    public void shouldUploadMultipartPartAndRefreshSessionCount() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartPartCount = 1;
        MultipartUploadServiceImpl service = service(dao);
        MultipartUploadPart part = multipartPart(1);

        MultipartUploadPart saved = service.uploadPart(toUploadMultipartPartCommand(part));

        assertNotNull(saved.getId());
        assertSame(saved, dao.insertedMultipartPart);
        assertSame(MultipartUploadStatus.UPLOADING, dao.updatedMultipartSession.getUploadStatus());
        assertEquals(Integer.valueOf(1), dao.updatedMultipartSession.getUploadedPartCount());
    }

    @Test(expected = BizException.class)
    public void shouldRejectDuplicateMultipartPartNumber() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartPartResult = multipartPart(1);

        service(dao).uploadPart(toUploadMultipartPartCommand(multipartPart(1)));
    }

    @Test(expected = BizException.class)
    public void shouldRejectClosedMultipartSessionWhenUploadingPart() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartSessionResult.setUploadStatus(MultipartUploadStatus.COMPLETED);

        service(dao).uploadPart(toUploadMultipartPartCommand(multipartPart(1)));
    }

    @Test
    public void shouldCompleteMultipartUploadAndCreateStorage() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartParts = Arrays.asList(multipartPart(1), multipartPart(2), multipartPart(3));
        CompleteMultipartUploadCommand command = new CompleteMultipartUploadCommand();
        command.setUploadId("upload-1");
        command.setStorageType(StorageType.LOCAL_FILE);
        command.setBucketName("/tmp/storage/");
        command.setObjectKey("202605/demo.png");
        command.setSize(300L);
        command.setAccessEndpoint("/api/storage/object/s1/content");

        StoredObject storage = service(dao).complete(command);

        assertNotNull(storage.getId());
        assertEquals("demo", storage.getName());
        assertEquals("png", storage.getExtendName());
        assertEquals("image/png", storage.getMimeType());
        assertEquals("owner-1", storage.getOwnerId());
        assertSame(StorageOwnerType.USER, storage.getOwnerType());
        assertSame(StorageType.LOCAL_FILE, storage.getStorageType());
        assertEquals("202605/demo.png", storage.getObjectKey());
        assertEquals(Long.valueOf(300L), storage.getSize());
        assertEquals("/api/storage/object/s1/content", storage.getAccessEndpoint());
        assertSame(StoredObjectStatus.ACTIVE, storage.getObjectStatus());
        assertSame(StoredObjectReferenceStatus.UNREFERENCED, storage.getReferenceStatus());
        assertSame(storage, dao.inserted);
        assertSame(MultipartUploadStatus.COMPLETED, dao.updatedMultipartSession.getUploadStatus());
        assertEquals(Integer.valueOf(3), dao.updatedMultipartSession.getUploadedPartCount());
        assertNotNull(dao.updatedMultipartSession.getCompletedDate());
    }

    @Test(expected = BizException.class)
    public void shouldRejectCompletingMultipartUploadWhenPartIsMissing() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartParts = Arrays.asList(multipartPart(1), multipartPart(3));

        service(dao).complete(new CompleteMultipartUploadCommand("upload-1", null, null, null, null, null));
    }

    @Test
    public void shouldAbortMultipartUpload() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();

        int count = service(dao).abort(new AbortMultipartUploadCommand("upload-1"));

        assertEquals(1, count);
        assertSame(MultipartUploadStatus.ABORTED, dao.updatedMultipartSession.getUploadStatus());
        assertNotNull(dao.updatedMultipartSession.getAbortedDate());
    }

    private static MultipartUploadServiceImpl service(RecordingMultipartUploadDao dao) {
        return new MultipartUploadServiceImpl(dao, dao);
    }

    private static MultipartUploadSession multipartSession() {
        MultipartUploadSession session = new MultipartUploadSession();
        session.setUploadId("upload-1");
        session.setOwnerId("owner-1");
        session.setOwnerType(StorageOwnerType.USER);
        session.setOriginalFilename("demo.png");
        session.setMimeType("image/png");
        session.setStorageType(StorageType.LOCAL_FILE);
        session.setTotalSize(300L);
        session.setPartSize(100L);
        session.setUploadStatus(MultipartUploadStatus.INITIATED);
        return session;
    }

    private static MultipartUploadPart multipartPart(int partNumber) {
        MultipartUploadPart part = new MultipartUploadPart();
        part.setUploadId("upload-1");
        part.setPartNumber(partNumber);
        part.setEtag("etag-" + partNumber);
        part.setSize(100L);
        return part;
    }

    private static InitMultipartUploadCommand toInitMultipartUploadCommand(MultipartUploadSession session) {
        InitMultipartUploadCommand command = new InitMultipartUploadCommand();
        command.setUploadId(session.getUploadId());
        command.setOwnerId(session.getOwnerId());
        command.setOwnerType(session.getOwnerType());
        command.setBusinessType(session.getBusinessType());
        command.setOriginalFilename(session.getOriginalFilename());
        command.setMimeType(session.getMimeType());
        command.setStorageType(session.getStorageType());
        command.setBucketName(session.getBucketName());
        command.setObjectKey(session.getObjectKey());
        command.setProviderUploadId(session.getProviderUploadId());
        command.setTotalSize(session.getTotalSize());
        command.setPartSize(session.getPartSize());
        return command;
    }

    private static UploadMultipartPartCommand toUploadMultipartPartCommand(MultipartUploadPart part) {
        UploadMultipartPartCommand command = new UploadMultipartPartCommand();
        command.setUploadId(part.getUploadId());
        command.setPartNumber(part.getPartNumber());
        command.setEtag(part.getEtag());
        command.setSize(part.getSize());
        return command;
    }

    private static class RecordingMultipartUploadDao implements MultipartUploadDao, StoredObjectDao {
        private StoredObject inserted;
        private MultipartUploadSession insertedMultipartSession;
        private MultipartUploadSession multipartSessionResult;
        private MultipartUploadSession updatedMultipartSession;
        private MultipartUploadPart insertedMultipartPart;
        private MultipartUploadPart multipartPartResult;
        private List<MultipartUploadPart> multipartParts = new ArrayList<>();
        private int multipartPartCount;

        @Override
        public MultipartUploadSessionId insertMultipartSession(MultipartUploadSession session) {
            this.insertedMultipartSession = session;
            return MultipartUploadSessionId.of(9301L);
        }

        @Override
        public MultipartUploadSession getMultipartSessionByUploadId(String uploadId) {
            return multipartSessionResult;
        }

        @Override
        public int updateMultipartSession(MultipartUploadSession session) {
            this.updatedMultipartSession = session;
            return 1;
        }

        @Override
        public MultipartUploadPartId insertMultipartPart(MultipartUploadPart part) {
            this.insertedMultipartPart = part;
            return MultipartUploadPartId.of(9401L);
        }

        @Override
        public MultipartUploadPart getMultipartPart(String uploadId, Integer partNumber) {
            return multipartPartResult;
        }

        @Override
        public List<MultipartUploadPart> listMultipartParts(String uploadId) {
            return multipartParts;
        }

        @Override
        public int countMultipartParts(String uploadId) {
            return multipartPartCount;
        }

        @Override
        public StoredObject getById(StoredObjectId id) {
            return null;
        }

        @Override
        public List<StoredObject> listByIds(List<Long> idList) {
            return null;
        }

        @Override
        public List<StoredObject> list(
                String mimeType,
                String ownerId,
                String ownerType,
                String objectStatus,
                String referenceStatus,
                String referenceOwnerId,
                String referenceOwnerType,
                String name,
                String remarks) {
            return null;
        }

        @Override
        public Page<StoredObject> page(
                String mimeType,
                String ownerId,
                String ownerType,
                String objectStatus,
                String referenceStatus,
                String referenceOwnerId,
                String referenceOwnerType,
                String name,
                String remarks,
                int pageNo,
                int pageSize) {
            return null;
        }

        @Override
        public StoredObjectId insert(StoredObject entity) {
            this.inserted = entity;
            return StoredObjectId.of(9101L);
        }

        @Override
        public int update(StoredObject entity) {
            return 1;
        }

        @Override
        public int deleteById(StoredObjectId id) {
            return 1;
        }

        @Override
        public List<String> listMimeTypes() {
            return null;
        }

        @Override
        public int updateObjectStatus(StoredObject storage) {
            return 1;
        }

        @Override
        public int updateReferenceStatus(StoredObject storage) {
            return 1;
        }
    }
}
