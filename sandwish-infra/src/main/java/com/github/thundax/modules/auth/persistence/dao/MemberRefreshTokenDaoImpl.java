package com.github.thundax.modules.auth.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.auth.dao.MemberRefreshTokenDao;
import com.github.thundax.modules.auth.entity.MemberRefreshToken;
import com.github.thundax.modules.auth.entity.enums.MemberRefreshTokenStatus;
import com.github.thundax.modules.auth.persistence.assembler.MemberRefreshTokenPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.MemberRefreshTokenDO;
import com.github.thundax.modules.auth.persistence.mapper.MemberRefreshTokenMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRefreshTokenDaoImpl implements MemberRefreshTokenDao {
    private final MemberRefreshTokenMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public MemberRefreshTokenDaoImpl(MemberRefreshTokenMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public MemberRefreshToken getById(EntityId id) {
        return MemberRefreshTokenPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public MemberRefreshToken getByTokenId(String tokenId) {
        LambdaQueryWrapper<MemberRefreshTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberRefreshTokenDO::getTokenId, tokenId);
        return MemberRefreshTokenPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public MemberRefreshToken getByTokenHash(String tokenHash) {
        LambdaQueryWrapper<MemberRefreshTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberRefreshTokenDO::getTokenHash, tokenHash);
        return MemberRefreshTokenPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<MemberRefreshToken> listByMemberIdAndStatus(EntityId memberId, MemberRefreshTokenStatus status) {
        LambdaQueryWrapper<MemberRefreshTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberRefreshTokenDO::getMemberId, EntityIdCodec.toValue(memberId));
        if (status != null) {
            wrapper.eq(MemberRefreshTokenDO::getStatus, status.value());
        }
        wrapper.orderByDesc(MemberRefreshTokenDO::getIssuedAt);
        return MemberRefreshTokenPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public EntityId insert(MemberRefreshToken refreshToken) {
        MemberRefreshTokenDO dataObject = MemberRefreshTokenPersistenceAssembler.toDataObject(refreshToken);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(MemberRefreshToken refreshToken) {
        MemberRefreshTokenDO dataObject = MemberRefreshTokenPersistenceAssembler.toDataObject(refreshToken);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberRefreshTokenDO::getStatus, dataObject.getStatus())
                        .set(MemberRefreshTokenDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(MemberRefreshTokenDO::getUpdateBy, dataObject.getUpdateBy()));
    }

    @Override
    public int updateStatus(MemberRefreshToken refreshToken) {
        MemberRefreshTokenDO dataObject = MemberRefreshTokenPersistenceAssembler.toDataObject(refreshToken);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(MemberRefreshTokenDO::getStatus, dataObject.getStatus()));
    }

    private LambdaUpdateWrapper<MemberRefreshTokenDO> buildIdUpdateWrapper(MemberRefreshTokenDO dataObject) {
        LambdaUpdateWrapper<MemberRefreshTokenDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MemberRefreshTokenDO::getId, dataObject.getId());
        return wrapper;
    }
}
