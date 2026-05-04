package com.github.thundax.modules.auth.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.dao.OAuthRefreshTokenDao;
import com.github.thundax.modules.auth.entity.OAuthRefreshToken;
import com.github.thundax.modules.auth.entity.enums.OAuthRefreshTokenStatus;
import com.github.thundax.modules.auth.persistence.assembler.OAuthRefreshTokenPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.OAuthRefreshTokenDO;
import com.github.thundax.modules.auth.persistence.mapper.OAuthRefreshTokenMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class OAuthRefreshTokenDaoImpl implements OAuthRefreshTokenDao {

    private final OAuthRefreshTokenMapper mapper;

    public OAuthRefreshTokenDaoImpl(OAuthRefreshTokenMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public OAuthRefreshToken getById(EntityId id) {
        return OAuthRefreshTokenPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public OAuthRefreshToken getByTokenId(String tokenId) {
        LambdaQueryWrapper<OAuthRefreshTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthRefreshTokenDO::getTokenId, tokenId);
        return OAuthRefreshTokenPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public OAuthRefreshToken getByTokenHash(String tokenHash) {
        LambdaQueryWrapper<OAuthRefreshTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthRefreshTokenDO::getTokenHash, tokenHash);
        return OAuthRefreshTokenPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<OAuthRefreshToken> listByClientIdAndUserIdAndStatus(
            String clientId, EntityId userId, OAuthRefreshTokenStatus status) {
        LambdaQueryWrapper<OAuthRefreshTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthRefreshTokenDO::getClientId, clientId);
        wrapper.eq(OAuthRefreshTokenDO::getUserId, EntityIdCodec.toValue(userId));
        if (status != null) {
            wrapper.eq(OAuthRefreshTokenDO::getStatus, status.value());
        }
        wrapper.orderByDesc(OAuthRefreshTokenDO::getIssuedAt);
        return OAuthRefreshTokenPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public String insert(OAuthRefreshToken refreshToken) {
        OAuthRefreshTokenDO dataObject = OAuthRefreshTokenPersistenceAssembler.toDataObject(refreshToken);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int updateStatus(OAuthRefreshToken refreshToken) {
        OAuthRefreshTokenDO dataObject = OAuthRefreshTokenPersistenceAssembler.toDataObject(refreshToken);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(OAuthRefreshTokenDO::getStatus, dataObject.getStatus())
                        .set(OAuthRefreshTokenDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(OAuthRefreshTokenDO::getUpdateBy, dataObject.getUpdateBy()));
    }

    private LambdaUpdateWrapper<OAuthRefreshTokenDO> buildIdUpdateWrapper(OAuthRefreshTokenDO dataObject) {
        LambdaUpdateWrapper<OAuthRefreshTokenDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OAuthRefreshTokenDO::getId, dataObject.getId());
        return wrapper;
    }
}
