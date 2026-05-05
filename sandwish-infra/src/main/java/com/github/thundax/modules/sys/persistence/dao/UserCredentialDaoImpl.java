package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.dao.UserCredentialDao;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.entity.enums.UserCredentialStatus;
import com.github.thundax.modules.sys.entity.enums.UserCredentialType;
import com.github.thundax.modules.sys.persistence.assembler.UserCredentialPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.UserCredentialDO;
import com.github.thundax.modules.sys.persistence.mapper.UserCredentialMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class UserCredentialDaoImpl implements UserCredentialDao {

    private final UserCredentialMapper mapper;

    public UserCredentialDaoImpl(UserCredentialMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserCredential getById(EntityId id) {
        return UserCredentialPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public UserCredential getByIdentityIdAndType(EntityId identityId, UserCredentialType credentialType) {
        LambdaQueryWrapper<UserCredentialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCredentialDO::getIdentityId, EntityIdCodec.toValue(identityId));
        wrapper.eq(UserCredentialDO::getCredentialType, credentialType.value());
        return UserCredentialPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public UserCredential getByUserIdAndType(EntityId userId, UserCredentialType credentialType) {
        LambdaQueryWrapper<UserCredentialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCredentialDO::getUserId, EntityIdCodec.toValue(userId));
        wrapper.eq(UserCredentialDO::getCredentialType, credentialType.value());
        return UserCredentialPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<UserCredential> listByUserIdAndStatus(EntityId userId, UserCredentialStatus status) {
        LambdaQueryWrapper<UserCredentialDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCredentialDO::getUserId, EntityIdCodec.toValue(userId));
        if (status != null) {
            wrapper.eq(UserCredentialDO::getStatus, status.value());
        }
        wrapper.orderByDesc(UserCredentialDO::getId);
        return UserCredentialPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public String insert(UserCredential userCredential) {
        UserCredentialDO dataObject = UserCredentialPersistenceAssembler.toDataObject(userCredential);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(UserCredential userCredential) {
        UserCredentialDO dataObject = UserCredentialPersistenceAssembler.toDataObject(userCredential);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(UserCredentialDO::getUserId, dataObject.getUserId())
                        .set(UserCredentialDO::getIdentityId, dataObject.getIdentityId())
                        .set(UserCredentialDO::getCredentialType, dataObject.getCredentialType())
                        .set(UserCredentialDO::getCredentialValue, dataObject.getCredentialValue())
                        .set(UserCredentialDO::getStatus, dataObject.getStatus())
                        .set(UserCredentialDO::getNeedChangePassword, dataObject.getNeedChangePassword())
                        .set(UserCredentialDO::getFailedCount, dataObject.getFailedCount())
                        .set(UserCredentialDO::getFailedLimit, dataObject.getFailedLimit())
                        .set(UserCredentialDO::getLockedUntil, dataObject.getLockedUntil())
                        .set(UserCredentialDO::getExpiresAt, dataObject.getExpiresAt())
                        .set(UserCredentialDO::getLastVerifiedAt, dataObject.getLastVerifiedAt()));
    }

    @Override
    public int updateStatus(UserCredential userCredential) {
        UserCredentialDO dataObject = UserCredentialPersistenceAssembler.toDataObject(userCredential);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(UserCredentialDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int updateVerifyState(UserCredential userCredential) {
        UserCredentialDO dataObject = UserCredentialPersistenceAssembler.toDataObject(userCredential);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(UserCredentialDO::getStatus, dataObject.getStatus())
                        .set(UserCredentialDO::getFailedCount, dataObject.getFailedCount())
                        .set(UserCredentialDO::getLockedUntil, dataObject.getLockedUntil())
                        .set(UserCredentialDO::getLastVerifiedAt, dataObject.getLastVerifiedAt()));
    }

    private LambdaUpdateWrapper<UserCredentialDO> buildIdUpdateWrapper(UserCredentialDO dataObject) {
        LambdaUpdateWrapper<UserCredentialDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserCredentialDO::getId, dataObject.getId());
        return wrapper;
    }
}
