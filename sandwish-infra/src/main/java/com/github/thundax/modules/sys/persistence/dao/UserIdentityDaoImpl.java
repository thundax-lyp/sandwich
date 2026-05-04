package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.dao.UserIdentityDao;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import com.github.thundax.modules.sys.persistence.assembler.UserIdentityPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.UserIdentityDO;
import com.github.thundax.modules.sys.persistence.mapper.UserIdentityMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class UserIdentityDaoImpl implements UserIdentityDao {

    private final UserIdentityMapper mapper;

    public UserIdentityDaoImpl(UserIdentityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserIdentity getById(EntityId id) {
        return UserIdentityPersistenceAssembler.toEntity(mapper.selectById(EntityIdCodec.toValue(id)));
    }

    @Override
    public UserIdentity getByIdentity(UserIdentityType identityType, String identityValue) {
        LambdaQueryWrapper<UserIdentityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserIdentityDO::getIdentityType, identityType.value());
        wrapper.eq(UserIdentityDO::getIdentityValue, identityValue);
        return UserIdentityPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public UserIdentity getByUserIdAndType(EntityId userId, UserIdentityType identityType) {
        LambdaQueryWrapper<UserIdentityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserIdentityDO::getUserId, EntityIdCodec.toValue(userId));
        wrapper.eq(UserIdentityDO::getIdentityType, identityType.value());
        return UserIdentityPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<UserIdentity> listByUserIdAndStatus(EntityId userId, UserIdentityStatus status) {
        LambdaQueryWrapper<UserIdentityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserIdentityDO::getUserId, EntityIdCodec.toValue(userId));
        if (status != null) {
            wrapper.eq(UserIdentityDO::getStatus, status.value());
        }
        wrapper.orderByDesc(UserIdentityDO::getCreateDate);
        return UserIdentityPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public String insert(UserIdentity userIdentity) {
        UserIdentityDO dataObject = UserIdentityPersistenceAssembler.toDataObject(userIdentity);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(UserIdentity userIdentity) {
        UserIdentityDO dataObject = UserIdentityPersistenceAssembler.toDataObject(userIdentity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(UserIdentityDO::getUserId, dataObject.getUserId())
                        .set(UserIdentityDO::getIdentityType, dataObject.getIdentityType())
                        .set(UserIdentityDO::getIdentityValue, dataObject.getIdentityValue())
                        .set(UserIdentityDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int updateStatus(UserIdentity userIdentity) {
        UserIdentityDO dataObject = UserIdentityPersistenceAssembler.toDataObject(userIdentity);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(UserIdentityDO::getStatus, dataObject.getStatus()));
    }

    private LambdaUpdateWrapper<UserIdentityDO> buildIdUpdateWrapper(UserIdentityDO dataObject) {
        LambdaUpdateWrapper<UserIdentityDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserIdentityDO::getId, dataObject.getId());
        return wrapper;
    }
}
