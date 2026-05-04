package com.github.thundax.modules.auth.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.dao.AuthSessionDao;
import com.github.thundax.modules.auth.entity.AuthSession;
import com.github.thundax.modules.auth.entity.enums.AuthSessionStatus;
import com.github.thundax.modules.auth.persistence.assembler.AuthSessionPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.AuthSessionDO;
import com.github.thundax.modules.auth.persistence.mapper.AuthSessionMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AuthSessionDaoImpl implements AuthSessionDao {

    private final AuthSessionMapper mapper;

    public AuthSessionDaoImpl(AuthSessionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AuthSession getById(EntityId id) {
        return AuthSessionPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public AuthSession getBySessionId(String sessionId) {
        LambdaQueryWrapper<AuthSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthSessionDO::getSessionId, sessionId);
        return AuthSessionPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public AuthSession getByToken(String token) {
        LambdaQueryWrapper<AuthSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthSessionDO::getToken, token);
        return AuthSessionPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<AuthSession> listByUserIdAndStatus(EntityId userId, AuthSessionStatus status) {
        LambdaQueryWrapper<AuthSessionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthSessionDO::getUserId, EntityIdCodec.toValue(userId));
        if (status != null) {
            wrapper.eq(AuthSessionDO::getStatus, status.value());
        }
        wrapper.orderByDesc(AuthSessionDO::getIssuedAt);
        return AuthSessionPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public String insert(AuthSession authSession) {
        AuthSessionDO dataObject = AuthSessionPersistenceAssembler.toDataObject(authSession);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int updateAccessTime(AuthSession authSession) {
        AuthSessionDO dataObject = AuthSessionPersistenceAssembler.toDataObject(authSession);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject).set(AuthSessionDO::getLastAccessTime, dataObject.getLastAccessTime()));
    }

    @Override
    public int updateLogout(AuthSession authSession) {
        AuthSessionDO dataObject = AuthSessionPersistenceAssembler.toDataObject(authSession);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(AuthSessionDO::getStatus, dataObject.getStatus())
                        .set(AuthSessionDO::getLastAccessTime, dataObject.getLastAccessTime())
                        .set(AuthSessionDO::getLogoutAt, dataObject.getLogoutAt()));
    }

    @Override
    public int updateInvalidate(AuthSession authSession) {
        AuthSessionDO dataObject = AuthSessionPersistenceAssembler.toDataObject(authSession);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(AuthSessionDO::getStatus, dataObject.getStatus())
                        .set(AuthSessionDO::getInvalidateReason, dataObject.getInvalidateReason()));
    }

    @Override
    public int updateExpire(AuthSession authSession) {
        AuthSessionDO dataObject = AuthSessionPersistenceAssembler.toDataObject(authSession);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(AuthSessionDO::getStatus, dataObject.getStatus()));
    }

    private LambdaUpdateWrapper<AuthSessionDO> buildIdUpdateWrapper(AuthSessionDO dataObject) {
        LambdaUpdateWrapper<AuthSessionDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuthSessionDO::getId, dataObject.getId());
        return wrapper;
    }
}
