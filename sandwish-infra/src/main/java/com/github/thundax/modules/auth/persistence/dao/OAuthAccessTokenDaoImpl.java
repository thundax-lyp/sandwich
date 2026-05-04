package com.github.thundax.modules.auth.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.dao.OAuthAccessTokenDao;
import com.github.thundax.modules.auth.entity.OAuthAccessToken;
import com.github.thundax.modules.auth.persistence.assembler.OAuthAccessTokenPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.OAuthAccessTokenDO;
import com.github.thundax.modules.auth.persistence.mapper.OAuthAccessTokenMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OAuthAccessTokenDaoImpl implements OAuthAccessTokenDao {

    private final OAuthAccessTokenMapper mapper;

    public OAuthAccessTokenDaoImpl(OAuthAccessTokenMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public OAuthAccessToken getById(EntityId id) {
        return OAuthAccessTokenPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public OAuthAccessToken getByTokenId(String tokenId) {
        LambdaQueryWrapper<OAuthAccessTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthAccessTokenDO::getTokenId, tokenId);
        return OAuthAccessTokenPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public OAuthAccessToken getByTokenHash(String tokenHash) {
        LambdaQueryWrapper<OAuthAccessTokenDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthAccessTokenDO::getTokenHash, tokenHash);
        return OAuthAccessTokenPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public String insert(OAuthAccessToken accessToken) {
        OAuthAccessTokenDO dataObject = OAuthAccessTokenPersistenceAssembler.toDataObject(accessToken);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int updateStatus(OAuthAccessToken accessToken) {
        OAuthAccessTokenDO dataObject = OAuthAccessTokenPersistenceAssembler.toDataObject(accessToken);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(OAuthAccessTokenDO::getStatus, dataObject.getStatus())
                        .set(OAuthAccessTokenDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(OAuthAccessTokenDO::getUpdateBy, dataObject.getUpdateBy()));
    }

    private LambdaUpdateWrapper<OAuthAccessTokenDO> buildIdUpdateWrapper(OAuthAccessTokenDO dataObject) {
        LambdaUpdateWrapper<OAuthAccessTokenDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OAuthAccessTokenDO::getId, dataObject.getId());
        return wrapper;
    }
}
