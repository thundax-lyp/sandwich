package com.github.thundax.modules.member.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.member.dao.MemberIdentityDao;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberIdentityStatus;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import com.github.thundax.modules.member.persistence.assembler.MemberIdentityPersistenceAssembler;
import com.github.thundax.modules.member.persistence.dataobject.MemberIdentityDO;
import com.github.thundax.modules.member.persistence.mapper.MemberIdentityMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MemberIdentityDaoImpl implements MemberIdentityDao {

    private final MemberIdentityMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public MemberIdentityDaoImpl(MemberIdentityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public MemberIdentity getById(EntityId id) {
        return MemberIdentityPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public MemberIdentity getByIdentity(MemberIdentityType identityType, String identityValue) {
        LambdaQueryWrapper<MemberIdentityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberIdentityDO::getIdentityType, identityType.value());
        wrapper.eq(MemberIdentityDO::getIdentityValue, identityValue);
        return MemberIdentityPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public MemberIdentity getByMemberIdAndType(EntityId memberId, MemberIdentityType identityType) {
        LambdaQueryWrapper<MemberIdentityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberIdentityDO::getMemberId, EntityIdCodec.toValue(memberId));
        wrapper.eq(MemberIdentityDO::getIdentityType, identityType.value());
        return MemberIdentityPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<MemberIdentity> listByMemberIdAndStatus(EntityId memberId, MemberIdentityStatus status) {
        LambdaQueryWrapper<MemberIdentityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberIdentityDO::getMemberId, EntityIdCodec.toValue(memberId));
        if (status != null) {
            wrapper.eq(MemberIdentityDO::getStatus, status.value());
        }
        wrapper.orderByDesc(MemberIdentityDO::getId);
        return MemberIdentityPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public EntityId insert(MemberIdentity memberIdentity) {
        MemberIdentityDO dataObject = MemberIdentityPersistenceAssembler.toDataObject(memberIdentity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(MemberIdentity memberIdentity) {
        MemberIdentityDO dataObject = MemberIdentityPersistenceAssembler.toDataObject(memberIdentity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberIdentityDO::getMemberId, dataObject.getMemberId())
                        .set(MemberIdentityDO::getIdentityType, dataObject.getIdentityType())
                        .set(MemberIdentityDO::getIdentityValue, dataObject.getIdentityValue())
                        .set(MemberIdentityDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int updateStatus(MemberIdentity memberIdentity) {
        MemberIdentityDO dataObject = MemberIdentityPersistenceAssembler.toDataObject(memberIdentity);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(MemberIdentityDO::getStatus, dataObject.getStatus()));
    }

    private LambdaUpdateWrapper<MemberIdentityDO> buildIdUpdateWrapper(MemberIdentityDO dataObject) {
        LambdaUpdateWrapper<MemberIdentityDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MemberIdentityDO::getId, dataObject.getId());
        return wrapper;
    }
}
