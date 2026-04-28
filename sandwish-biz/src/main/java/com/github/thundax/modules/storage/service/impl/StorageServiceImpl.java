package com.github.thundax.modules.storage.service.impl;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.storage.dao.StorageDao;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.service.StorageService;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** @author wdit */
@Service
@Transactional(readOnly = true)
public class StorageServiceImpl implements StorageService {

    private final StorageDao dao;

    public StorageServiceImpl(StorageDao dao) {
        this.dao = dao;
    }

    @Override
    public Storage get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return dao.get(id);
    }

    @Override
    public List<Storage> getMany(List<String> ids) {
        return dao.getMany(ids);
    }

    @Override
    public List<Storage> findList(Storage storage) {
        Storage.Query query = storage == null ? null : storage.getQuery();
        return dao.findList(
                query == null ? null : query.getMimeType(),
                query == null ? null : query.getOwnerId(),
                query == null ? null : query.getOwnerType(),
                query == null ? null : query.getEnableFlag(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks());
    }

    @Override
    public Page<Storage> findPage(Storage storage, Page<Storage> page) {
        Storage.Query query = storage == null ? null : storage.getQuery();
        return dao.findPage(
                query == null ? null : query.getMimeType(),
                query == null ? null : query.getOwnerId(),
                query == null ? null : query.getOwnerType(),
                query == null ? null : query.getEnableFlag(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks(),
                page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Storage storage) {
        if (storage.getIsNewRecord()) {
            storage.preInsert();
            dao.insert(storage);
        } else {
            storage.preUpdate();
            dao.update(storage);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Storage storage) {
        if (storage == null) {
            return 0;
        }
        return dao.delete(storage.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(List<Storage> list) {
        return batchOperate(list, this::delete);
    }

    @Override
    public List<String> findMimeTypeList() {
        return dao.findMimeTypeList();
    }

    @Override
    public List<String> findBusinessTypeList() {
        return dao.findBusinessTypeList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(Storage storage) {
        return dao.updateEnableFlag(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePublicFlag(Storage storage) {
        return dao.updatePublicFlag(storage);
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
    public List<StorageBusiness> findBusiness(Storage entity) {
        return dao.findBusiness(entity);
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
}
