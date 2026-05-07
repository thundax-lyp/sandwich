package com.github.thundax.modules.auth.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.auth.dao.MemberAuthSessionDao;
import com.github.thundax.modules.auth.entity.MemberAuthSession;
import com.github.thundax.modules.auth.entity.enums.MemberAuthSessionStatus;
import com.github.thundax.modules.auth.persistence.assembler.MemberAuthSessionPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.MemberAuthSessionDO;
import com.github.thundax.modules.auth.persistence.mapper.MemberAuthSessionMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MemberAuthSessionDaoImpl implements MemberAuthSessionDao {
    private final MemberAuthSessionMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public MemberAuthSessionDaoImpl(MemberAuthSessionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public MemberAuthSession getById(EntityId id) {
        return MemberAuthSessionPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public MemberAuthSession getBySessionId(String sessionId) {
        LambdaQueryWrapper<MemberAuthSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberAuthSessionDO::getSessionId, sessionId);
        return MemberAuthSessionPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<MemberAuthSession> listByMemberIdAndStatus(EntityId memberId, MemberAuthSessionStatus status) {
        LambdaQueryWrapper<MemberAuthSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberAuthSessionDO::getMemberId, EntityIdCodec.toValue(memberId));
        if (status != null) {
            wrapper.eq(MemberAuthSessionDO::getStatus, status.value());
        }
        wrapper.orderByDesc(MemberAuthSessionDO::getIssuedAt);
        return MemberAuthSessionPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public EntityId insert(MemberAuthSession authSession) {
        MemberAuthSessionDO dataObject = MemberAuthSessionPersistenceAssembler.toDataObject(authSession);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(MemberAuthSession authSession) {
        MemberAuthSessionDO dataObject = MemberAuthSessionPersistenceAssembler.toDataObject(authSession);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberAuthSessionDO::getStatus, dataObject.getStatus())
                        .set(MemberAuthSessionDO::getLastAccessTime, dataObject.getLastAccessTime())
                        .set(MemberAuthSessionDO::getLogoutAt, dataObject.getLogoutAt())
                        .set(MemberAuthSessionDO::getInvalidateReason, dataObject.getInvalidateReason())
                        .set(MemberAuthSessionDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(MemberAuthSessionDO::getUpdateBy, dataObject.getUpdateBy()));
    }

    @Override
    public int updateStatus(MemberAuthSession authSession) {
        MemberAuthSessionDO dataObject = MemberAuthSessionPersistenceAssembler.toDataObject(authSession);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(MemberAuthSessionDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int touch(MemberAuthSession authSession) {
        MemberAuthSessionDO dataObject = MemberAuthSessionPersistenceAssembler.toDataObject(authSession);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberAuthSessionDO::getLastAccessTime, dataObject.getLastAccessTime()));
    }

    private LambdaUpdateWrapper<MemberAuthSessionDO> buildIdUpdateWrapper(MemberAuthSessionDO dataObject) {
        LambdaUpdateWrapper<MemberAuthSessionDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MemberAuthSessionDO::getId, dataObject.getId());
        return wrapper;
    }
}
