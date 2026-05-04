package com.github.thundax.modules.storage.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.dao.StorageBusinessDao;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import com.github.thundax.modules.storage.persistence.assembler.StoragePersistenceAssembler;
import com.github.thundax.modules.storage.persistence.dataobject.StorageBusinessDO;
import com.github.thundax.modules.storage.persistence.mapper.StorageBusinessMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class StorageBusinessDaoImpl implements StorageBusinessDao {

    private final StorageBusinessMapper mapper;

    public StorageBusinessDaoImpl(StorageBusinessMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<String> listBusinessTypes() {
        return toStringList(mapper.selectObjs(new QueryWrapper<StorageBusinessDO>()
                .select("business_type")
                .groupBy("business_type")
                .orderByAsc("business_type")));
    }

    @Override
    public List<StorageBusiness> listBusiness(Storage entity) {
        LambdaQueryWrapper<StorageBusinessDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageBusinessDO::getFileId, EntityIdCodec.toValue(entity.getId()));
        return StoragePersistenceAssembler.toBusinessEntityList(mapper.selectList(wrapper));
    }

    @Override
    public void insertBusiness(List<StorageBusiness> list) {
        List<StorageBusinessDO> dataObjects = StoragePersistenceAssembler.toBusinessDataObjectList(list);
        if (dataObjects == null) {
            return;
        }
        for (StorageBusinessDO dataObject : dataObjects) {
            mapper.insert(dataObject);
        }
    }

    @Override
    public void deleteBusiness(String id) {
        LambdaQueryWrapper<StorageBusinessDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageBusinessDO::getFileId, id);
        mapper.delete(wrapper);
    }

    @Override
    public int deleteBusinessByBusiness(String businessType, String businessId) {
        LambdaQueryWrapper<StorageBusinessDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StorageBusinessDO::getBusinessType, businessType);
        wrapper.eq(StorageBusinessDO::getBusinessId, businessId);
        return mapper.delete(wrapper);
    }

    private List<String> toStringList(List<Object> objects) {
        List<String> values = new ArrayList<>();
        if (objects == null) {
            return values;
        }
        for (Object object : objects) {
            if (object != null) {
                values.add(String.valueOf(object));
            }
        }
        return values;
    }
}
