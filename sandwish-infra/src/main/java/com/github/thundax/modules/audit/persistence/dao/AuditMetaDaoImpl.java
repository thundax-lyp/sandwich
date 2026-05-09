package com.github.thundax.modules.audit.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.audit.dao.AuditMetaDao;
import com.github.thundax.modules.audit.entity.AuditMeta;
import com.github.thundax.modules.audit.entity.valueobject.AuditObjectRef;
import com.github.thundax.modules.audit.persistence.assembler.AuditMetaPersistenceAssembler;
import com.github.thundax.modules.audit.persistence.dataobject.AuditMetaDO;
import com.github.thundax.modules.audit.persistence.mapper.AuditMetaMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AuditMetaDaoImpl implements AuditMetaDao {

    private final AuditMetaMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AuditMetaDaoImpl(AuditMetaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AuditMeta getByObjectRef(AuditObjectRef objectRef) {
        if (objectRef == null || !objectRef.isValid()) {
            return null;
        }
        LambdaQueryWrapper<AuditMetaDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditMetaDO::getObjectType, objectRef.getObjectType());
        wrapper.eq(AuditMetaDO::getObjectId, objectRef.getObjectId());
        return AuditMetaPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public EntityId insert(AuditMeta meta) {
        AuditMetaDO dataObject = AuditMetaPersistenceAssembler.toDataObject(meta);
        dataObject.setId(idGenerator.nextId().value());
        if (dataObject.getLastLogId() == null) {
            dataObject.setLastLogId(0L);
        }
        if (dataObject.getCreatedLogId() == null) {
            dataObject.setCreatedLogId(0L);
        }
        mapper.insert(dataObject);
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(AuditMeta meta) {
        return mapper.updateById(AuditMetaPersistenceAssembler.toDataObject(meta));
    }
}
