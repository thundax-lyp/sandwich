package com.github.thundax.modules.storage.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.exception.BizException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.storage.dao.MultipartUploadDao;
import com.github.thundax.modules.storage.dao.StoredObjectDao;
import com.github.thundax.modules.storage.dao.StoredObjectReferenceDao;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.entity.enums.MultipartUploadStatus;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class StorageServiceImplTest {

    @Test
    public void shouldGetStorageById() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        StoredObject expected = storage(8101L);
        dao.getResult = expected;

        StorageServiceImpl service = storageService(dao);

        assertSame(expected, service.getById(EntityId.of(8101L)));
        assertEquals(Long.valueOf(8101L), dao.id);
    }

    @Test
    public void shouldExpandFindPageQuery() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        StorageQuery query = new StorageQuery();
        query.setContentType("image/png");
        query.setOwnerId("owner-1");
        query.setOwnerType(StorageOwnerType.USER);
        query.setObjectStatus(StoredObjectStatus.ACTIVE);
        query.setReferenceStatus(StoredObjectReferenceStatus.REFERENCED);
        query.setReferenceOwnerId("business-1");
        query.setReferenceOwnerType("Article");
        query.setOriginalFilename("avatar");
        query.setRemarks("remark");
        PageDTO<StoredObject> page = new PageDTO<>(2, 20);

        StorageServiceImpl service = storageService(dao);
        service.page(query, page);

        assertEquals("image/png", dao.mimeType);
        assertEquals("owner-1", dao.ownerId);
        assertEquals("USER", dao.ownerType);
        assertEquals("ACTIVE", dao.objectStatus);
        assertEquals("REFERENCED", dao.referenceStatus);
        assertEquals("business-1", dao.referenceOwnerId);
        assertEquals("Article", dao.referenceOwnerType);
        assertEquals("avatar", dao.name);
        assertEquals("remark", dao.remarks);
        assertEquals(page.getPageNo(), dao.pageNo);
        assertEquals(page.getPageSize(), dao.pageSize);
    }

    @Test
    public void shouldPrepareEntityBeforeSave() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        StoredObject storage = new StoredObject();

        StorageServiceImpl service = storageService(dao);
        service.add(storage);

        assertNotNull(storage.getId());
        assertEquals(null, storage.getCreateDate());
        assertEquals(null, storage.getUpdateDate());
        assertSame(storage, dao.inserted);
    }

    @Test
    public void shouldBatchDeleteById() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        StorageServiceImpl service = storageService(dao);

        int count = service.batchDeleteById(Arrays.asList(EntityId.of(8101L), EntityId.of(8102L)));

        assertEquals(2, count);
        assertEquals(Arrays.asList(8101L, 8102L), dao.deletedIds);
    }

    @Test
    public void shouldDelegateBusinessOperations() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        StorageServiceImpl service = storageService(dao);
        List<StoredObjectReference> list = Arrays.asList(storageBusiness(8201L));

        service.addReferences(list);
        service.removeReferences(StorageOwnerType.USER, "u1");

        assertSame(list, dao.businessList);
        assertEquals("USER:u1", dao.deletedBusinessKey);
    }

    @Test
    public void shouldAllowPublicStorageAccess() {
        StorageServiceImpl service = storageService(new RecordingStoredObjectDao());
        StoredObject storage = storage(8101L);
        storage.setReferenceStatus(StoredObjectReferenceStatus.REFERENCED);

        assertTrue(service.canReadContent(storage, null, null));
    }

    @Test
    public void shouldAllowPrivateStorageOwnerAccess() {
        StorageServiceImpl service = storageService(new RecordingStoredObjectDao());
        StoredObject storage = storage(8101L);
        storage.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
        storage.setOwnerType(StorageOwnerType.USER);
        storage.setOwnerId("u1");

        assertTrue(service.canReadContent(storage, StorageOwnerType.USER, "u1"));
    }

    @Test
    public void shouldDenyPrivateStorageAccessForOtherOwner() {
        StorageServiceImpl service = storageService(new RecordingStoredObjectDao());
        StoredObject storage = storage(8101L);
        storage.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
        storage.setOwnerType(StorageOwnerType.USER);
        storage.setOwnerId("u1");

        assertFalse(service.canReadContent(storage, StorageOwnerType.USER, "u2"));
        assertFalse(service.canReadContent(storage, StorageOwnerType.MEMBER, "u1"));
        assertFalse(service.canReadContent(storage, StorageOwnerType.USER, null));
        assertFalse(service.canReadContent(null, StorageOwnerType.USER, "u1"));
    }

    @Test
    public void shouldInitMultipartUploadSession() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        StorageServiceImpl service = storageService(dao);
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
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartPartCount = 1;
        StorageServiceImpl service = storageService(dao);
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
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartPartResult = multipartPart(1);

        storageService(dao).uploadMultipartPart(multipartPart(1));
    }

    @Test(expected = BizException.class)
    public void shouldRejectClosedMultipartSessionWhenUploadingPart() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartSessionResult.setUploadStatus(MultipartUploadStatus.COMPLETED);

        storageService(dao).uploadMultipartPart(multipartPart(1));
    }

    @Test
    public void shouldCompleteMultipartUploadAndCreateStorage() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartParts = Arrays.asList(multipartPart(1), multipartPart(2), multipartPart(3));
        StoredObject object = new StoredObject();
        object.setStorageType(StorageType.LOCAL_FILE);
        object.setBucketName("/tmp/storage/");
        object.setObjectKey("202605/demo.png");
        object.setSize(300L);
        object.setAccessEndpoint("/api/assist/storage/objects/s1/content");

        StoredObject storage = storageService(dao).completeMultipartUpload("upload-1", object);

        assertNotNull(storage.getId());
        assertEquals("demo", storage.getName());
        assertEquals("png", storage.getExtendName());
        assertEquals("image/png", storage.getMimeType());
        assertEquals("owner-1", storage.getOwnerId());
        assertSame(StorageOwnerType.USER, storage.getOwnerType());
        assertSame(StorageType.LOCAL_FILE, storage.getStorageType());
        assertEquals("202605/demo.png", storage.getObjectKey());
        assertEquals(Long.valueOf(300L), storage.getSize());
        assertEquals("/api/assist/storage/objects/s1/content", storage.getAccessEndpoint());
        assertSame(StoredObjectStatus.ACTIVE, storage.getObjectStatus());
        assertSame(StoredObjectReferenceStatus.UNREFERENCED, storage.getReferenceStatus());
        assertSame(storage, dao.inserted);
        assertSame(MultipartUploadStatus.COMPLETED, dao.updatedMultipartSession.getUploadStatus());
        assertEquals(Integer.valueOf(3), dao.updatedMultipartSession.getUploadedPartCount());
        assertNotNull(dao.updatedMultipartSession.getCompletedDate());
    }

    @Test(expected = BizException.class)
    public void shouldRejectCompletingMultipartUploadWhenPartIsMissing() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartParts = Arrays.asList(multipartPart(1), multipartPart(3));

        storageService(dao).completeMultipartUpload("upload-1", null);
    }

    @Test
    public void shouldAbortMultipartUpload() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        dao.multipartSessionResult = multipartSession();

        int count = storageService(dao).abortMultipartUpload("upload-1");

        assertEquals(1, count);
        assertSame(MultipartUploadStatus.ABORTED, dao.updatedMultipartSession.getUploadStatus());
        assertNotNull(dao.updatedMultipartSession.getAbortedDate());
    }

    private static StoredObject storage(Long id) {
        StoredObject storage = new StoredObject();
        storage.setId(EntityIdCodec.toDomain(id));
        return storage;
    }

    private static StoredObjectReference storageBusiness(Long id) {
        StoredObjectReference storageBusiness = new StoredObjectReference();
        storageBusiness.setId(EntityIdCodec.toDomain(id));
        return storageBusiness;
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

    private static StorageServiceImpl storageService(RecordingStoredObjectDao dao) {
        return new StorageServiceImpl(dao, dao, dao);
    }

    private static class RecordingStoredObjectDao
            implements StoredObjectDao, StoredObjectReferenceDao, MultipartUploadDao {

        private StoredObject getResult;
        private Long id;
        private String mimeType;
        private String ownerId;
        private String ownerType;
        private String objectStatus;
        private String referenceStatus;
        private String referenceOwnerId;
        private String referenceOwnerType;
        private String name;
        private String remarks;
        private int pageNo;
        private int pageSize;
        private StoredObject inserted;
        private List<Long> deletedIds = new java.util.ArrayList<>();
        private List<StoredObjectReference> businessList;
        private String deletedBusinessKey;
        private MultipartUploadSession insertedMultipartSession;
        private MultipartUploadSession multipartSessionResult;
        private MultipartUploadSession updatedMultipartSession;
        private MultipartUploadPart insertedMultipartPart;
        private MultipartUploadPart multipartPartResult;
        private List<MultipartUploadPart> multipartParts = new ArrayList<>();
        private int multipartPartCount;

        @Override
        public StoredObject getById(EntityId id) {
            this.id = id.value();
            return getResult;
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
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<StoredObject> page(
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
            this.mimeType = mimeType;
            this.ownerId = ownerId;
            this.ownerType = ownerType;
            this.objectStatus = objectStatus;
            this.referenceStatus = referenceStatus;
            this.referenceOwnerId = referenceOwnerId;
            this.referenceOwnerType = referenceOwnerType;
            this.name = name;
            this.remarks = remarks;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
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
            deletedIds.add(id.value());
            return 1;
        }

        @Override
        public List<String> listMimeTypes() {
            return null;
        }

        @Override
        public List<String> listReferenceOwnerTypes() {
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

        @Override
        public List<StoredObjectReference> listReferences(StoredObject entity) {
            return null;
        }

        @Override
        public void insertReferences(List<StoredObjectReference> list) {
            this.businessList = list;
        }

        @Override
        public void deleteByObjectId(String id) {}

        @Override
        public int deleteByOwner(String referenceOwnerType, String referenceOwnerId) {
            this.deletedBusinessKey = referenceOwnerType + ":" + referenceOwnerId;
            return 1;
        }

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
    }
}
