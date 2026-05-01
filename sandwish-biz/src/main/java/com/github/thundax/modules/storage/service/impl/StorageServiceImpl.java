package com.github.thundax.modules.storage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.storage.dao.StorageDao;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageStatus;
import com.github.thundax.modules.storage.entity.enums.StorageVisibility;
import com.github.thundax.modules.storage.service.StorageService;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StorageServiceImpl implements StorageService {

    private final StorageDao dao;

    public StorageServiceImpl(StorageDao dao) {
        this.dao = dao;
    }

    @Override
    public Storage getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Storage> batchGetByIds(List<String> ids) {
        return dao.batchGetByIds(ids);
    }

    @Override
    public List<Storage> list(Storage storage) {
        Storage.Query query = storage == null ? null : storage.getQuery();
        return dao.list(
                query == null ? null : query.getMimeType(),
                query == null ? null : query.getOwnerId(),
                query == null ? null : ownerTypeValue(query.getOwnerType()),
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : visibilityValue(query.getVisibility()),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks());
    }

    @Override
    public Page<Storage> page(Storage storage, Page<Storage> page) {
        Page<Storage> normalizedPage = normalizePage(page);
        Storage.Query query = storage == null ? null : storage.getQuery();
        IPage<Storage> dataPage = dao.page(
                query == null ? null : query.getMimeType(),
                query == null ? null : query.getOwnerId(),
                query == null ? null : ownerTypeValue(query.getOwnerType()),
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : visibilityValue(query.getVisibility()),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Storage storage) {
        storage.setId(EntityIdCodec.toDomain(dao.insert(storage)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Storage storage) {
        dao.update(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Storage storage) {
        if (storage == null) {
            return 0;
        }
        return dao.deleteById(storage.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<Storage> list) {
        return batchOperate(list, this::deleteById);
    }

    @Override
    public List<String> listMimeTypes() {
        return dao.listMimeTypes();
    }

    @Override
    public List<String> listBusinessTypes() {
        return dao.listBusinessTypes();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(Storage storage) {
        return dao.updateStatus(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateVisibility(Storage storage) {
        return dao.updateVisibility(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeBusiness(String businessType, String businessId) {
        return dao.deleteBusinessByBusiness(businessType, businessId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertBusiness(List<StorageBusiness> list) {
        dao.insertBusiness(list);
    }

    @Override
    public List<StorageBusiness> listBusiness(Storage entity) {
        return dao.listBusiness(entity);
    }

    private int batchOperate(Collection<Storage> collection, Function<Storage, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (Storage entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private Page<Storage> normalizePage(Page<Storage> page) {
        Page<Storage> normalizedPage = page == null ? new Page<>() : page;
        normalizedPage.initialize();
        return normalizedPage;
    }

    private String ownerTypeValue(StorageOwnerType ownerType) {
        return ownerType == null ? null : ownerType.value();
    }

    private String statusValue(StorageStatus status) {
        return status == null ? null : status.value();
    }

    private String visibilityValue(StorageVisibility visibility) {
        return visibility == null ? null : visibility.value();
    }
}
