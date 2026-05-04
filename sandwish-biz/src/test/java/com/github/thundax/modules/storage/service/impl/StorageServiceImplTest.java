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
import com.github.thundax.modules.storage.backend.StorageBackendObject;
import com.github.thundax.modules.storage.dao.MultipartUploadDao;
import com.github.thundax.modules.storage.dao.StorageDao;
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
        RecordingStorageDao dao = new RecordingStorageDao();
        StoredObject expected = storage("s1");
        dao.getResult = expected;

        StorageServiceImpl service = storageService(dao);

        assertSame(expected, service.getById(EntityId.of("s1")));
        assertEquals("s1", dao.id);
    }

    @Test
    public void shouldExpandFindPageQuery() {
        RecordingStorageDao dao = new RecordingStorageDao();
        StorageQuery query = new StorageQuery();
        query.setMimeType("image/png");
        query.setOwnerId("owner-1");
        query.setOwnerType(StorageOwnerType.USER);
        query.setStatus(StoredObjectStatus.ACTIVE);
        query.setVisibility(StoredObjectReferenceStatus.REFERENCED);
        query.setBusinessId("business-1");
        query.setBusinessType("Article");
        query.setName("avatar");
        query.setRemarks("remark");
        PageDTO<StoredObject> page = new PageDTO<>(2, 20);

        StorageServiceImpl service = storageService(dao);
        service.page(query, page);

        assertEquals("image/png", dao.mimeType);
        assertEquals("owner-1", dao.ownerId);
        assertEquals("USER", dao.ownerType);
        assertEquals("ACTIVE", dao.enableFlag);
        assertEquals("REFERENCED", dao.publicFlag);
        assertEquals("business-1", dao.businessId);
        assertEquals("Article", dao.businessType);
        assertEquals("avatar", dao.name);
        assertEquals("remark", dao.remarks);
        assertEquals(page.getPageNo(), dao.pageNo);
        assertEquals(page.getPageSize(), dao.pageSize);
    }

    @Test
    public void shouldPrepareEntityBeforeSave() {
        RecordingStorageDao dao = new RecordingStorageDao();
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
        RecordingStorageDao dao = new RecordingStorageDao();
        StorageServiceImpl service = storageService(dao);

        int count = service.batchDeleteById(Arrays.asList(EntityId.of("s1"), EntityId.of("s2")));

        assertEquals(2, count);
        assertEquals(Arrays.asList("s1", "s2"), dao.deletedIds);
    }

    @Test
    public void shouldDelegateBusinessOperations() {
        RecordingStorageDao dao = new RecordingStorageDao();
        StorageServiceImpl service = storageService(dao);
        List<StoredObjectReference> list = Arrays.asList(storageBusiness("s1"));

        service.insertBusiness(list);
        service.removeBusiness("User", "u1");

        assertSame(list, dao.businessList);
        assertEquals("User:u1", dao.deletedBusinessKey);
    }

    @Test
    public void shouldAllowPublicStorageAccess() {
        StorageServiceImpl service = storageService(new RecordingStorageDao());
        StoredObject storage = storage("s1");
        storage.setVisibility(StoredObjectReferenceStatus.REFERENCED);

        assertTrue(service.canAccess(storage, null, null));
    }

    @Test
    public void shouldAllowPrivateStorageOwnerAccess() {
        StorageServiceImpl service = storageService(new RecordingStorageDao());
        StoredObject storage = storage("s1");
        storage.setVisibility(StoredObjectReferenceStatus.UNREFERENCED);
        storage.setOwnerType(StorageOwnerType.USER);
        storage.setOwnerId("u1");

        assertTrue(service.canAccess(storage, StorageOwnerType.USER, "u1"));
    }

    @Test
    public void shouldDenyPrivateStorageAccessForOtherOwner() {
        StorageServiceImpl service = storageService(new RecordingStorageDao());
        StoredObject storage = storage("s1");
        storage.setVisibility(StoredObjectReferenceStatus.UNREFERENCED);
        storage.setOwnerType(StorageOwnerType.USER);
        storage.setOwnerId("u1");

        assertFalse(service.canAccess(storage, StorageOwnerType.USER, "u2"));
        assertFalse(service.canAccess(storage, StorageOwnerType.MEMBER, "u1"));
        assertFalse(service.canAccess(storage, StorageOwnerType.USER, null));
        assertFalse(service.canAccess(null, StorageOwnerType.USER, "u1"));
    }

    @Test
    public void shouldInitMultipartUploadSession() {
        RecordingStorageDao dao = new RecordingStorageDao();
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
        RecordingStorageDao dao = new RecordingStorageDao();
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
        RecordingStorageDao dao = new RecordingStorageDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartPartResult = multipartPart(1);

        storageService(dao).uploadMultipartPart(multipartPart(1));
    }

    @Test(expected = BizException.class)
    public void shouldRejectClosedMultipartSessionWhenUploadingPart() {
        RecordingStorageDao dao = new RecordingStorageDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartSessionResult.setUploadStatus(MultipartUploadStatus.COMPLETED);

        storageService(dao).uploadMultipartPart(multipartPart(1));
    }

    @Test
    public void shouldCompleteMultipartUploadAndCreateStorage() {
        RecordingStorageDao dao = new RecordingStorageDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartParts = Arrays.asList(multipartPart(1), multipartPart(2), multipartPart(3));
        StorageBackendObject object = new StorageBackendObject();
        object.setStorageType(StorageType.LOCAL_FILE);
        object.setBucketName("/tmp/storage/");
        object.setObjectKey("202605/demo.png");
        object.setSize(300L);
        object.setAccessEndpoint("/servlet/storage/demo.png");

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
        assertSame(storage, dao.inserted);
        assertSame(MultipartUploadStatus.COMPLETED, dao.updatedMultipartSession.getUploadStatus());
        assertEquals(Integer.valueOf(3), dao.updatedMultipartSession.getUploadedPartCount());
        assertNotNull(dao.updatedMultipartSession.getCompletedDate());
    }

    @Test(expected = BizException.class)
    public void shouldRejectCompletingMultipartUploadWhenPartIsMissing() {
        RecordingStorageDao dao = new RecordingStorageDao();
        dao.multipartSessionResult = multipartSession();
        dao.multipartParts = Arrays.asList(multipartPart(1), multipartPart(3));

        storageService(dao).completeMultipartUpload("upload-1", null);
    }

    @Test
    public void shouldAbortMultipartUpload() {
        RecordingStorageDao dao = new RecordingStorageDao();
        dao.multipartSessionResult = multipartSession();

        int count = storageService(dao).abortMultipartUpload("upload-1");

        assertEquals(1, count);
        assertSame(MultipartUploadStatus.ABORTED, dao.updatedMultipartSession.getUploadStatus());
        assertNotNull(dao.updatedMultipartSession.getAbortedDate());
    }

    private static StoredObject storage(String id) {
        StoredObject storage = new StoredObject();
        storage.setId(EntityIdCodec.toDomain(id));
        return storage;
    }

    private static StoredObjectReference storageBusiness(String id) {
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

    private static StorageServiceImpl storageService(RecordingStorageDao dao) {
        return new StorageServiceImpl(dao, dao, dao);
    }

    private static class RecordingStorageDao implements StorageDao, StoredObjectReferenceDao, MultipartUploadDao {

        private StoredObject getResult;
        private String id;
        private String mimeType;
        private String ownerId;
        private String ownerType;
        private String enableFlag;
        private String publicFlag;
        private String businessId;
        private String businessType;
        private String name;
        private String remarks;
        private int pageNo;
        private int pageSize;
        private StoredObject inserted;
        private List<String> deletedIds = new java.util.ArrayList<>();
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
        public List<StoredObject> listByIds(List<String> idList) {
            return null;
        }

        @Override
        public List<StoredObject> list(
                String mimeType,
                String ownerId,
                String ownerType,
                String enableFlag,
                String publicFlag,
                String businessId,
                String businessType,
                String name,
                String remarks) {
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<StoredObject> page(
                String mimeType,
                String ownerId,
                String ownerType,
                String enableFlag,
                String publicFlag,
                String businessId,
                String businessType,
                String name,
                String remarks,
                int pageNo,
                int pageSize) {
            this.mimeType = mimeType;
            this.ownerId = ownerId;
            this.ownerType = ownerType;
            this.enableFlag = enableFlag;
            this.publicFlag = publicFlag;
            this.businessId = businessId;
            this.businessType = businessType;
            this.name = name;
            this.remarks = remarks;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
        }

        @Override
        public String insert(StoredObject entity) {
            this.inserted = entity;
            return "generated-storage-id";
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
        public List<String> listBusinessTypes() {
            return null;
        }

        @Override
        public int updateStatus(StoredObject storage) {
            return 1;
        }

        @Override
        public int updateVisibility(StoredObject storage) {
            return 1;
        }

        @Override
        public List<StoredObjectReference> listBusiness(StoredObject entity) {
            return null;
        }

        @Override
        public void insertBusiness(List<StoredObjectReference> list) {
            this.businessList = list;
        }

        @Override
        public void deleteBusiness(String id) {}

        @Override
        public int deleteBusinessByBusiness(String businessType, String businessId) {
            this.deletedBusinessKey = businessType + ":" + businessId;
            return 1;
        }

        @Override
        public String insertMultipartSession(MultipartUploadSession session) {
            this.insertedMultipartSession = session;
            return "generated-session-id";
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
        public String insertMultipartPart(MultipartUploadPart part) {
            this.insertedMultipartPart = part;
            return "generated-part-id";
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
