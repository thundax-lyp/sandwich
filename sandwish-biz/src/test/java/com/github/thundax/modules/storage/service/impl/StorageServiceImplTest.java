package com.github.thundax.modules.storage.service.impl;

import static org.junit.Assert.*;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.modules.storage.dao.StoredObjectDao;
import com.github.thundax.modules.storage.dao.StoredObjectReferenceDao;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.service.command.AddStorageReferencesCommand;
import com.github.thundax.modules.storage.service.command.CreateStorageCommand;
import com.github.thundax.modules.storage.service.command.DeleteStorageCommand;
import com.github.thundax.modules.storage.service.command.RemoveStorageReferencesCommand;
import com.github.thundax.modules.storage.service.query.StorageQuery;
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

        assertSame(expected, service.get(StoredObjectId.of(8101L)));
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
        PageQuery page = new PageQuery(2, 20);

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
        storage.setId(service.create(toCreateStorageCommand(storage)));

        assertNotNull(storage.getId());
        assertEquals(storage.getId(), dao.inserted.getId());
    }

    @Test
    public void shouldBatchDeleteById() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        StorageServiceImpl service = storageService(dao);

        int count = service.remove(new DeleteStorageCommand(StoredObjectId.of(8101L)))
                + service.remove(new DeleteStorageCommand(StoredObjectId.of(8102L)));

        assertEquals(2, count);
        assertEquals(Arrays.asList(8101L, 8102L), dao.deletedIds);
    }

    @Test
    public void shouldDelegateBusinessOperations() {
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        StorageServiceImpl service = storageService(dao);
        List<StoredObjectReference> list = Arrays.asList(storageBusiness(8201L));

        service.addReferences(new AddStorageReferencesCommand(list));
        service.removeReferences(new RemoveStorageReferencesCommand(StorageOwnerType.USER, "u1"));

        assertSame(list, dao.businessList);
        assertEquals("USER:u1", dao.deletedBusinessKey);
    }

    @Test
    public void shouldAllowPublicStorageAccess() {
        StoredObject storage = storage(8101L);
        storage.setReferenceStatus(StoredObjectReferenceStatus.REFERENCED);
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        dao.getResult = storage;
        StorageServiceImpl service = storageService(dao);

        assertTrue(service.existsReadableContent(storageQuery(8101L, null, null)));
    }

    @Test
    public void shouldAllowPrivateStorageOwnerAccess() {
        StoredObject storage = storage(8101L);
        storage.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
        storage.setOwnerType(StorageOwnerType.USER);
        storage.setOwnerId("u1");
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        dao.getResult = storage;
        StorageServiceImpl service = storageService(dao);

        assertTrue(service.existsReadableContent(storageQuery(8101L, StorageOwnerType.USER, "u1")));
    }

    @Test
    public void shouldDenyPrivateStorageAccessForOtherOwner() {
        StoredObject storage = storage(8101L);
        storage.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
        storage.setOwnerType(StorageOwnerType.USER);
        storage.setOwnerId("u1");
        RecordingStoredObjectDao dao = new RecordingStoredObjectDao();
        dao.getResult = storage;
        StorageServiceImpl service = storageService(dao);

        assertFalse(service.existsReadableContent(storageQuery(8101L, StorageOwnerType.USER, "u2")));
        assertFalse(service.existsReadableContent(storageQuery(8101L, StorageOwnerType.MEMBER, "u1")));
        assertFalse(service.existsReadableContent(storageQuery(8101L, StorageOwnerType.USER, null)));
        dao.getResult = null;
        assertFalse(service.existsReadableContent(storageQuery(8101L, StorageOwnerType.USER, "u1")));
    }

    private static StoredObject storage(Long id) {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectIdCodec.toDomain(id));
        return storage;
    }

    private static StoredObjectReference storageBusiness(Long id) {
        StoredObjectReference storageBusiness = new StoredObjectReference();
        storageBusiness.setId(StoredObjectIdCodec.toDomain(id));
        return storageBusiness;
    }

    private static StorageServiceImpl storageService(RecordingStoredObjectDao dao) {
        return new StorageServiceImpl(dao, dao);
    }

    private static StorageQuery storageQuery(Long id, StorageOwnerType ownerType, String ownerId) {
        StorageQuery query = new StorageQuery();
        query.setId(StoredObjectIdCodec.toDomain(id));
        query.setOwnerType(ownerType);
        query.setOwnerId(ownerId);
        return query;
    }

    private static CreateStorageCommand toCreateStorageCommand(StoredObject storage) {
        CreateStorageCommand command = new CreateStorageCommand();
        command.setId(storage.getId());
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeType());
        command.setOwnerId(storage.getOwnerId());
        command.setOwnerType(storage.getOwnerType());
        command.setStorageType(storage.getStorageType());
        command.setBucketName(storage.getBucketName());
        command.setObjectKey(storage.getObjectKey());
        command.setSize(storage.getSize());
        command.setAccessEndpoint(storage.getAccessEndpoint());
        command.setObjectStatus(storage.getObjectStatus());
        command.setReferenceStatus(storage.getReferenceStatus());
        command.setPriority(storage.getPriority());
        command.setRemarks(storage.getRemarks());
        return command;
    }

    private static class RecordingStoredObjectDao implements StoredObjectDao, StoredObjectReferenceDao {

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

        @Override
        public StoredObject getById(StoredObjectId id) {
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
    }
}
