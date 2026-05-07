package com.github.thundax.modules.storage.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.exception.BizException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.dao.MultipartUploadDao;
import com.github.thundax.modules.storage.dao.StoredObjectDao;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.MultipartUploadStatus;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
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

        MultipartUploadSession saved = service.initMultipartUpload(session);

        assertSame(session, saved);
        assertNotNull(saved.getId());
        assertNotNull(saved.getUploadId());
        assertEquals(Integer.valueOf(0), saved.getUploadedPartCount());
        assertSame(MultipartUploadStatus.INITIATED, saved.getUploadStatus());
        assertNotNull(saved.getCreateDate());
        assertNotNull(saved.getUpdateDate());
        assertSame(session, dao.insertedMultipartSession);
    }

    @Test
    public void shouldUploadMultipartPartAndRefreshSessionCount() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartPartCount = 1;
        MultipartUploadServiceImpl service = service(dao);
        MultipartUploadPart part = multipartPart(1);

        MultipartUploadPart saved = service.uploadMultipartPart(part);

        assertSame(part, saved);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreateDate());
        assertSame(part, dao.insertedMultipartPart);
        assertSame(MultipartUploadStatus.UPLOADING, dao.updatedMultipartSession.getUploadStatus());
        assertEquals(Integer.valueOf(1), dao.updatedMultipartSession.getUploadedPartCount());
    }

    @Test(expected = BizException.class)
    public void shouldRejectDuplicateMultipartPartNumber() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartPartResult = multipartPart(1);

        service(dao).uploadMultipartPart(multipartPart(1));
    }

    @Test(expected = BizException.class)
    public void shouldRejectClosedMultipartSessionWhenUploadingPart() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartSessionResult.setUploadStatus(MultipartUploadStatus.COMPLETED);

        service(dao).uploadMultipartPart(multipartPart(1));
    }

    @Test
    public void shouldCompleteMultipartUploadAndCreateStorage() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartParts = Arrays.asList(multipartPart(1), multipartPart(2), multipartPart(3));
        StoredObject object = new StoredObject();
        object.setStorageType(StorageType.LOCAL_FILE);
        object.setBucketName("/tmp/storage/");
        object.setObjectKey("202605/demo.png");
        object.setSize(300L);
        object.setAccessEndpoint("/api/storage/objects/s1/content");

        StoredObject storage = service(dao).completeMultipartUpload("upload-1", object);

        assertNotNull(storage.getId());
        assertEquals("demo", storage.getName());
        assertEquals("png", storage.getExtendName());
        assertEquals("image/png", storage.getMimeType());
        assertEquals("owner-1", storage.getOwnerId());
        assertSame(StorageOwnerType.USER, storage.getOwnerType());
        assertSame(StorageType.LOCAL_FILE, storage.getStorageType());
        assertEquals("202605/demo.png", storage.getObjectKey());
        assertEquals(Long.valueOf(300L), storage.getSize());
        assertEquals("/api/storage/objects/s1/content", storage.getAccessEndpoint());
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

        service(dao).completeMultipartUpload("upload-1", null);
    }

    @Test
    public void shouldAbortMultipartUpload() {
        RecordingMultipartUploadDao dao = new RecordingMultipartUploadDao();
        dao.multipartSessionResult = multipartSession();

        int count = service(dao).abortMultipartUpload("upload-1");

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
        public EntityId insertMultipartSession(MultipartUploadSession session) {
            this.insertedMultipartSession = session;
            return EntityId.of(9301L);
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
        public EntityId insertMultipartPart(MultipartUploadPart part) {
            this.insertedMultipartPart = part;
            return EntityId.of(9401L);
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
        public StoredObject getById(EntityId id) {
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
        public EntityId insert(StoredObject entity) {
            this.inserted = entity;
            return EntityId.of(9101L);
        }

        @Override
        public int update(StoredObject entity) {
            return 1;
        }

        @Override
        public int deleteById(EntityId id) {
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
