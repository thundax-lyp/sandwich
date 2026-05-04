package com.github.thundax.modules.storage.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.dao.StoredObjectReferenceDao;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.persistence.assembler.StoragePersistenceAssembler;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectReferenceDO;
import com.github.thundax.modules.storage.persistence.mapper.StoredObjectReferenceMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class StoredObjectReferenceDaoImpl implements StoredObjectReferenceDao {

    private final StoredObjectReferenceMapper mapper;

    public StoredObjectReferenceDaoImpl(StoredObjectReferenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<String> listReferenceOwnerTypes() {
        return toStringList(mapper.selectObjs(new QueryWrapper<StoredObjectReferenceDO>()
                .select("business_type")
                .groupBy("business_type")
                .orderByAsc("business_type")));
    }

    @Override
    public List<StoredObjectReference> listReferences(StoredObject entity) {
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getFileId, EntityIdCodec.toValue(entity.getId()));
        return StoragePersistenceAssembler.toBusinessEntityList(mapper.selectList(wrapper));
    }

    @Override
    public void insertReferences(List<StoredObjectReference> list) {
        List<StoredObjectReferenceDO> dataObjects = StoragePersistenceAssembler.toBusinessDataObjectList(list);
        if (dataObjects == null) {
            return;
        }
        for (StoredObjectReferenceDO dataObject : dataObjects) {
            mapper.insert(dataObject);
        }
    }

    @Override
    public void deleteByObjectId(String id) {
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getFileId, id);
        mapper.delete(wrapper);
    }

    @Override
    public int deleteByOwner(String referenceOwnerType, String referenceOwnerId) {
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerType, referenceOwnerType);
        wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerId, referenceOwnerId);
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
