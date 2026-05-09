package com.github.thundax.modules.audit.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.modules.audit.dao.AuditLogDao;
import com.github.thundax.modules.audit.entity.AuditLog;
import com.github.thundax.modules.audit.persistence.assembler.AuditLogPersistenceAssembler;
import com.github.thundax.modules.audit.persistence.dataobject.AuditLogDO;
import com.github.thundax.modules.audit.persistence.mapper.AuditLogMapper;
import com.github.thundax.modules.audit.service.query.AuditLogQuery;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogDaoImpl implements AuditLogDao {

    private final AuditLogMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AuditLogDaoImpl(AuditLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public EntityId insert(AuditLog log) {
        AuditLogDO dataObject = AuditLogPersistenceAssembler.toDataObject(log);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public AuditLog getByIdempotencyKey(String idempotencyKey) {
        if (StringUtils.isBlank(idempotencyKey)) {
            return null;
        }
        LambdaQueryWrapper<AuditLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDO::getIdempotencyKey, idempotencyKey);
        return AuditLogPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<AuditLog> listByObject(String objectType, String objectId) {
        LambdaQueryWrapper<AuditLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDO::getObjectType, objectType);
        wrapper.eq(AuditLogDO::getObjectId, objectId);
        wrapper.orderByDesc(AuditLogDO::getVersion);
        return AuditLogPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public Page<AuditLog> page(AuditLogQuery query, PageQuery pageQuery) {
        LambdaQueryWrapper<AuditLogDO> wrapper = buildWrapper(query);
        wrapper.orderByDesc(AuditLogDO::getOccurredAt, AuditLogDO::getId);
        Page<AuditLogDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize()), wrapper);
        Page<AuditLog> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(AuditLogPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    private LambdaQueryWrapper<AuditLogDO> buildWrapper(AuditLogQuery query) {
        LambdaQueryWrapper<AuditLogDO> wrapper = new LambdaQueryWrapper<>();
        if (query == null) {
            return wrapper;
        }
        wrapper.eq(StringUtils.isNotBlank(query.getObjectType()), AuditLogDO::getObjectType, query.getObjectType());
        wrapper.eq(StringUtils.isNotBlank(query.getObjectId()), AuditLogDO::getObjectId, query.getObjectId());
        wrapper.eq(
                query.getAction() != null,
                AuditLogDO::getAction,
                query.getAction() == null ? null : query.getAction().value());
        wrapper.eq(
                query.getOperatorType() != null,
                AuditLogDO::getOperatorType,
                query.getOperatorType() == null ? null : query.getOperatorType().value());
        wrapper.eq(StringUtils.isNotBlank(query.getOperatorId()), AuditLogDO::getOperatorId, query.getOperatorId());
        wrapper.eq(StringUtils.isNotBlank(query.getSource()), AuditLogDO::getSource, query.getSource());
        wrapper.eq(StringUtils.isNotBlank(query.getRequestId()), AuditLogDO::getRequestId, query.getRequestId());
        wrapper.ge(query.getBeginDate() != null, AuditLogDO::getOccurredAt, query.getBeginDate());
        wrapper.le(query.getEndDate() != null, AuditLogDO::getOccurredAt, query.getEndDate());
        return wrapper;
    }
}
