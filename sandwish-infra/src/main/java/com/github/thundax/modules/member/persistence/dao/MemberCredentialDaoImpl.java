package com.github.thundax.modules.member.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.member.dao.MemberCredentialDao;
import com.github.thundax.modules.member.entity.MemberCredential;
import com.github.thundax.modules.member.entity.enums.MemberCredentialStatus;
import com.github.thundax.modules.member.entity.enums.MemberCredentialType;
import com.github.thundax.modules.member.persistence.assembler.MemberCredentialPersistenceAssembler;
import com.github.thundax.modules.member.persistence.dataobject.MemberCredentialDO;
import com.github.thundax.modules.member.persistence.mapper.MemberCredentialMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MemberCredentialDaoImpl implements MemberCredentialDao {

    private final MemberCredentialMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public MemberCredentialDaoImpl(MemberCredentialMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public MemberCredential getById(EntityId id) {
        return MemberCredentialPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public MemberCredential getByIdentityIdAndType(EntityId identityId, MemberCredentialType credentialType) {
        LambdaQueryWrapper<MemberCredentialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCredentialDO::getIdentityId, EntityIdCodec.toValue(identityId));
        wrapper.eq(MemberCredentialDO::getCredentialType, credentialType.value());
        return MemberCredentialPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public MemberCredential getByMemberIdAndType(EntityId memberId, MemberCredentialType credentialType) {
        LambdaQueryWrapper<MemberCredentialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCredentialDO::getMemberId, EntityIdCodec.toValue(memberId));
        wrapper.eq(MemberCredentialDO::getCredentialType, credentialType.value());
        return MemberCredentialPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<MemberCredential> listByMemberIdAndStatus(EntityId memberId, MemberCredentialStatus status) {
        LambdaQueryWrapper<MemberCredentialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCredentialDO::getMemberId, EntityIdCodec.toValue(memberId));
        if (status != null) {
            wrapper.eq(MemberCredentialDO::getStatus, status.value());
        }
        wrapper.orderByDesc(MemberCredentialDO::getId);
        return MemberCredentialPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public EntityId insert(MemberCredential memberCredential) {
        MemberCredentialDO dataObject = MemberCredentialPersistenceAssembler.toDataObject(memberCredential);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(MemberCredential memberCredential) {
        MemberCredentialDO dataObject = MemberCredentialPersistenceAssembler.toDataObject(memberCredential);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberCredentialDO::getMemberId, dataObject.getMemberId())
                        .set(MemberCredentialDO::getIdentityId, dataObject.getIdentityId())
                        .set(MemberCredentialDO::getCredentialType, dataObject.getCredentialType())
                        .set(MemberCredentialDO::getCredentialValue, dataObject.getCredentialValue())
                        .set(MemberCredentialDO::getStatus, dataObject.getStatus())
                        .set(MemberCredentialDO::getNeedChangePassword, dataObject.getNeedChangePassword())
                        .set(MemberCredentialDO::getFailedCount, dataObject.getFailedCount())
                        .set(MemberCredentialDO::getFailedLimit, dataObject.getFailedLimit())
                        .set(MemberCredentialDO::getLockedUntil, dataObject.getLockedUntil())
                        .set(MemberCredentialDO::getExpiresAt, dataObject.getExpiresAt())
                        .set(MemberCredentialDO::getLastVerifiedAt, dataObject.getLastVerifiedAt()));
    }

    @Override
    public int updateStatus(MemberCredential memberCredential) {
        MemberCredentialDO dataObject = MemberCredentialPersistenceAssembler.toDataObject(memberCredential);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(MemberCredentialDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int updateVerifyState(MemberCredential memberCredential) {
        MemberCredentialDO dataObject = MemberCredentialPersistenceAssembler.toDataObject(memberCredential);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberCredentialDO::getStatus, dataObject.getStatus())
                        .set(MemberCredentialDO::getFailedCount, dataObject.getFailedCount())
                        .set(MemberCredentialDO::getLockedUntil, dataObject.getLockedUntil())
                        .set(MemberCredentialDO::getLastVerifiedAt, dataObject.getLastVerifiedAt()));
    }

    private LambdaUpdateWrapper<MemberCredentialDO> buildIdUpdateWrapper(MemberCredentialDO dataObject) {
        LambdaUpdateWrapper<MemberCredentialDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MemberCredentialDO::getId, dataObject.getId());
        return wrapper;
    }
}
