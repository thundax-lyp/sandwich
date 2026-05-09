package com.github.thundax.modules.storage.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.thundax.modules.storage.dao.StoredObjectReferenceDao;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.persistence.assembler.StoragePersistenceAssembler;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectReferenceDO;
import com.github.thundax.modules.storage.persistence.mapper.StoredObjectReferenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class StoredObjectReferenceDaoImpl implements StoredObjectReferenceDao {

    private final StoredObjectReferenceMapper mapper;

    public StoredObjectReferenceDaoImpl(StoredObjectReferenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<String> listReferenceOwnerTypes() {
        return mapper
                .selectObjs(new QueryWrapper<StoredObjectReferenceDO>()
                        .select("business_type")
                        .groupBy("business_type")
                        .orderByAsc("business_type"))
                .stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoredObjectReference> listReferences(StoredObject entity) {
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getFileId, entity.getId().value());
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
        wrapper.eq(StoredObjectReferenceDO::getFileId, Long.valueOf(id));
        mapper.delete(wrapper);
    }

    @Override
    public int deleteByOwner(String referenceOwnerType, String referenceOwnerId) {
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerType, referenceOwnerType);
        wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerId, referenceOwnerId);
        return mapper.delete(wrapper);
    }
}
