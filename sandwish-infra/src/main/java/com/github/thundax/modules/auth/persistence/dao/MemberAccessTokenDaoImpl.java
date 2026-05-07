package com.github.thundax.modules.auth.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.auth.dao.MemberAccessTokenDao;
import com.github.thundax.modules.auth.entity.MemberAccessToken;
import com.github.thundax.modules.auth.entity.enums.MemberAccessTokenStatus;
import com.github.thundax.modules.auth.persistence.assembler.MemberAccessTokenPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.MemberAccessTokenDO;
import com.github.thundax.modules.auth.persistence.mapper.MemberAccessTokenMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MemberAccessTokenDaoImpl implements MemberAccessTokenDao {
    private final MemberAccessTokenMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public MemberAccessTokenDaoImpl(MemberAccessTokenMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public MemberAccessToken getById(EntityId id) {
        return MemberAccessTokenPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public MemberAccessToken getByTokenId(String tokenId) {
        LambdaQueryWrapper<MemberAccessTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberAccessTokenDO::getTokenId, tokenId);
        return MemberAccessTokenPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public MemberAccessToken getByTokenHash(String tokenHash) {
        LambdaQueryWrapper<MemberAccessTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberAccessTokenDO::getTokenHash, tokenHash);
        return MemberAccessTokenPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<MemberAccessToken> listByMemberIdAndStatus(EntityId memberId, MemberAccessTokenStatus status) {
        LambdaQueryWrapper<MemberAccessTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberAccessTokenDO::getMemberId, EntityIdCodec.toValue(memberId));
        if (status != null) {
            wrapper.eq(MemberAccessTokenDO::getStatus, status.value());
        }
        wrapper.orderByDesc(MemberAccessTokenDO::getIssuedAt);
        return MemberAccessTokenPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public EntityId insert(MemberAccessToken accessToken) {
        MemberAccessTokenDO dataObject = MemberAccessTokenPersistenceAssembler.toDataObject(accessToken);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(MemberAccessToken accessToken) {
        MemberAccessTokenDO dataObject = MemberAccessTokenPersistenceAssembler.toDataObject(accessToken);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberAccessTokenDO::getStatus, dataObject.getStatus())
                        .set(MemberAccessTokenDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(MemberAccessTokenDO::getUpdateBy, dataObject.getUpdateBy()));
    }

    @Override
    public int updateStatus(MemberAccessToken accessToken) {
        MemberAccessTokenDO dataObject = MemberAccessTokenPersistenceAssembler.toDataObject(accessToken);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(MemberAccessTokenDO::getStatus, dataObject.getStatus()));
    }

    private LambdaUpdateWrapper<MemberAccessTokenDO> buildIdUpdateWrapper(MemberAccessTokenDO dataObject) {
        LambdaUpdateWrapper<MemberAccessTokenDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MemberAccessTokenDO::getId, dataObject.getId());
        return wrapper;
    }
}
