package com.github.thundax.modules.storage.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.storage.dao.StorageDao;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageStatus;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class StorageServiceImplTest {

    @Test
    public void shouldGetStorageById() {
        RecordingStorageDao dao = new RecordingStorageDao();
        Storage expected = storage("s1");
        dao.getResult = expected;

        StorageServiceImpl service = new StorageServiceImpl(dao);

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
        query.setStatus(StorageStatus.ENABLED);
        query.setVisibility(StorageVisibility.PUBLIC);
        query.setBusinessId("business-1");
        query.setBusinessType("Article");
        query.setName("avatar");
        query.setRemarks("remark");
        Page<Storage> page = new Page<>(2, 20);

        StorageServiceImpl service = new StorageServiceImpl(dao);
        service.page(query, page);

        assertEquals("image/png", dao.mimeType);
        assertEquals("owner-1", dao.ownerId);
        assertEquals("USER", dao.ownerType);
        assertEquals("ENABLED", dao.enableFlag);
        assertEquals("PUBLIC", dao.publicFlag);
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
        Storage storage = new Storage();

        StorageServiceImpl service = new StorageServiceImpl(dao);
        service.add(storage);

        assertNotNull(storage.getId());
        assertEquals(null, storage.getCreateDate());
        assertEquals(null, storage.getUpdateDate());
        assertSame(storage, dao.inserted);
    }

    @Test
    public void shouldBatchDeleteById() {
        RecordingStorageDao dao = new RecordingStorageDao();
        StorageServiceImpl service = new StorageServiceImpl(dao);

        int count = service.batchDeleteById(Arrays.asList(EntityId.of("s1"), EntityId.of("s2")));

        assertEquals(2, count);
        assertEquals(Arrays.asList("s1", "s2"), dao.deletedIds);
    }

    @Test
    public void shouldDelegateBusinessOperations() {
        RecordingStorageDao dao = new RecordingStorageDao();
        StorageServiceImpl service = new StorageServiceImpl(dao);
        List<StorageBusiness> list = Arrays.asList(storageBusiness("s1"));

        service.insertBusiness(list);
        service.removeBusiness("User", "u1");

        assertSame(list, dao.businessList);
        assertEquals("User:u1", dao.deletedBusinessKey);
    }

    @Test
    public void shouldAllowPublicStorageAccess() {
        StorageServiceImpl service = new StorageServiceImpl(new RecordingStorageDao());
        Storage storage = storage("s1");
        storage.setVisibility(StorageVisibility.PUBLIC);

        assertTrue(service.canAccess(storage, null, null));
    }

    @Test
    public void shouldAllowPrivateStorageOwnerAccess() {
        StorageServiceImpl service = new StorageServiceImpl(new RecordingStorageDao());
        Storage storage = storage("s1");
        storage.setVisibility(StorageVisibility.PRIVATE);
        storage.setOwnerType(StorageOwnerType.USER);
        storage.setOwnerId("u1");

        assertTrue(service.canAccess(storage, StorageOwnerType.USER, "u1"));
    }

    @Test
    public void shouldDenyPrivateStorageAccessForOtherOwner() {
        StorageServiceImpl service = new StorageServiceImpl(new RecordingStorageDao());
        Storage storage = storage("s1");
        storage.setVisibility(StorageVisibility.PRIVATE);
        storage.setOwnerType(StorageOwnerType.USER);
        storage.setOwnerId("u1");

        assertFalse(service.canAccess(storage, StorageOwnerType.USER, "u2"));
        assertFalse(service.canAccess(storage, StorageOwnerType.MEMBER, "u1"));
        assertFalse(service.canAccess(storage, StorageOwnerType.USER, null));
        assertFalse(service.canAccess(null, StorageOwnerType.USER, "u1"));
    }

    private static Storage storage(String id) {
        Storage storage = new Storage();
        storage.setId(EntityIdCodec.toDomain(id));
        return storage;
    }

    private static StorageBusiness storageBusiness(String id) {
        StorageBusiness storageBusiness = new StorageBusiness();
        storageBusiness.setId(EntityIdCodec.toDomain(id));
        return storageBusiness;
    }

    private static class RecordingStorageDao implements StorageDao {

        private Storage getResult;
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
        private Storage inserted;
        private List<String> deletedIds = new java.util.ArrayList<>();
        private List<StorageBusiness> businessList;
        private String deletedBusinessKey;

        @Override
        public Storage getById(EntityId id) {
            this.id = id.value();
            return getResult;
        }

        @Override
        public List<Storage> batchGetByIds(List<String> idList) {
            return null;
        }

        @Override
        public List<Storage> list(
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
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Storage> page(
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
        public String insert(Storage entity) {
            this.inserted = entity;
            return "generated-storage-id";
        }

        @Override
        public int update(Storage entity) {
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
        public int updateStatus(Storage storage) {
            return 1;
        }

        @Override
        public int updateVisibility(Storage storage) {
            return 1;
        }

        @Override
        public List<StorageBusiness> listBusiness(Storage entity) {
            return null;
        }

        @Override
        public void insertBusiness(List<StorageBusiness> list) {
            this.businessList = list;
        }

        @Override
        public void deleteBusiness(String id) {}

        @Override
        public int deleteBusinessByBusiness(String businessType, String businessId) {
            this.deletedBusinessKey = businessType + ":" + businessId;
            return 1;
        }
    }
}
