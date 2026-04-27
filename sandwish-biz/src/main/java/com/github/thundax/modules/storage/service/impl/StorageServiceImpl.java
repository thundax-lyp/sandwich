package com.github.thundax.modules.storage.service.impl;

import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.modules.storage.dao.StorageDao;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class StorageServiceImpl extends CrudServiceImpl<StorageDao, Storage> implements StorageService {

    public StorageServiceImpl(StorageDao dao) {
        super(dao);
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
}
