package com.github.thundax.modules.storage.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.storage.dao.StorageDao;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class StorageServiceImplTest {

    @Test
    public void shouldGetStorageById() {
        RecordingStorageDao dao = new RecordingStorageDao();
        Storage expected = new Storage("s1");
        dao.getResult = expected;

        StorageServiceImpl service = new StorageServiceImpl(dao);

        assertSame(expected, service.get("s1"));
        assertEquals("s1", dao.id);
    }

    @Test
    public void shouldExpandFindPageQuery() {
        RecordingStorageDao dao = new RecordingStorageDao();
        Storage query = new Storage();
        Storage.Query condition = new Storage.Query();
        condition.setMimeType("image/png");
        condition.setOwnerId("owner-1");
        condition.setOwnerType("user");
        condition.setEnableFlag("1");
        condition.setName("avatar");
        condition.setRemarks("remark");
        query.setQuery(condition);
        Page<Storage> page = new Page<>(2, 20);

        StorageServiceImpl service = new StorageServiceImpl(dao);
        service.findPage(query, page);

        assertEquals("image/png", dao.mimeType);
        assertEquals("owner-1", dao.ownerId);
        assertEquals("user", dao.ownerType);
        assertEquals("1", dao.enableFlag);
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
        service.save(storage);

        assertNotNull(storage.getId());
        assertNotNull(storage.getCreateDate());
        assertNotNull(storage.getUpdateDate());
        assertSame(storage, dao.inserted);
    }

    @Test
    public void shouldBatchDeleteById() {
        RecordingStorageDao dao = new RecordingStorageDao();
        StorageServiceImpl service = new StorageServiceImpl(dao);

        int count = service.delete(Arrays.asList(new Storage("s1"), new Storage("s2")));

        assertEquals(2, count);
        assertEquals(Arrays.asList("s1", "s2"), dao.deletedIds);
    }

    @Test
    public void shouldDelegateBusinessOperations() {
        RecordingStorageDao dao = new RecordingStorageDao();
        StorageServiceImpl service = new StorageServiceImpl(dao);
        List<StorageBusiness> list = Arrays.asList(new StorageBusiness("s1"));

        service.insertBusiness(list);
        service.removeBusiness("User", "u1");

        assertSame(list, dao.businessList);
        assertEquals("User:u1", dao.deletedBusinessKey);
    }

    private static class RecordingStorageDao implements StorageDao {

        private Storage getResult;
        private String id;
        private String mimeType;
        private String ownerId;
        private String ownerType;
        private String enableFlag;
        private String name;
        private String remarks;
        private int pageNo;
        private int pageSize;
        private Storage inserted;
        private List<String> deletedIds = new java.util.ArrayList<>();
        private List<StorageBusiness> businessList;
        private String deletedBusinessKey;

        @Override
        public Storage get(String id) {
            this.id = id;
            return getResult;
        }

        @Override
        public List<Storage> getMany(List<String> idList) {
            return null;
        }

        @Override
        public List<Storage> findList(
                String mimeType,
                String ownerId,
                String ownerType,
                String enableFlag,
                String name,
                String remarks) {
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Storage> findPage(
                String mimeType,
                String ownerId,
                String ownerType,
                String enableFlag,
                String name,
                String remarks,
                int pageNo,
                int pageSize) {
            this.mimeType = mimeType;
            this.ownerId = ownerId;
            this.ownerType = ownerType;
            this.enableFlag = enableFlag;
            this.name = name;
            this.remarks = remarks;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                    pageNo, pageSize);
        }

        @Override
        public int insert(Storage entity) {
            this.inserted = entity;
            return 1;
        }

        @Override
        public int update(Storage entity) {
            return 1;
        }

        @Override
        public int delete(String id) {
            deletedIds.add(id);
            return 1;
        }

        @Override
        public List<String> findMimeTypeList() {
            return null;
        }

        @Override
        public List<String> findBusinessTypeList() {
            return null;
        }

        @Override
        public int updateEnableFlag(Storage storage) {
            return 1;
        }

        @Override
        public int updatePublicFlag(Storage storage) {
            return 1;
        }

        @Override
        public List<StorageBusiness> findBusiness(Storage entity) {
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
