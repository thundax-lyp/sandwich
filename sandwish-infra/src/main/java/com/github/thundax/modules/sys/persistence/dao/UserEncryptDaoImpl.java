package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.dao.UserEncryptDao;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.persistence.assembler.UserEncryptPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.UserEncryptDO;
import com.github.thundax.modules.sys.persistence.mapper.UserEncryptMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class UserEncryptDaoImpl implements UserEncryptDao {

    private final UserEncryptMapper mapper;

    public UserEncryptDaoImpl(UserEncryptMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserEncrypt get(EntityId id) {
        return UserEncryptPersistenceAssembler.toEntity(mapper.selectById(id.value()));
    }

    @Override
    public List<UserEncrypt> getMany(List<String> idList) {
        return UserEncryptPersistenceAssembler.toEntityList(mapper.selectBatchIds(idList));
    }

    @Override
    public List<UserEncrypt> findList() {
        return UserEncryptPersistenceAssembler.toEntityList(mapper.selectList(new LambdaQueryWrapper<>()));
    }

    @Override
    public Page<UserEncrypt> findPage(int pageNo, int pageSize) {
        Page<UserEncryptDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), new LambdaQueryWrapper<>());
        Page<UserEncrypt> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(UserEncryptPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public int insert(UserEncrypt entity) {
        return mapper.insert(UserEncryptPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(UserEncrypt entity) {
        UserEncryptDO dataObject = UserEncryptPersistenceAssembler.toDataObject(entity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(UserEncryptDO::getEmail, dataObject.getEmail())
                        .set(UserEncryptDO::getMobile, dataObject.getMobile())
                        .set(UserEncryptDO::getTel, dataObject.getTel()));
    }

    @Override
    public int updatePriority(UserEncrypt entity) {
        return 0;
    }

    @Override
    public int delete(EntityId id) {
        return mapper.deleteById(id.value());
    }

    @Override
    public void updateLoginPass(UserEncrypt userEncrypt) {
        UserEncryptDO dataObject = UserEncryptPersistenceAssembler.toDataObject(userEncrypt);
        mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(UserEncryptDO::getLoginPass, dataObject.getLoginPass()));
    }

    private LambdaUpdateWrapper<UserEncryptDO> buildIdUpdateWrapper(UserEncryptDO dataObject) {
        LambdaUpdateWrapper<UserEncryptDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserEncryptDO::getUserId, dataObject.getUserId());
        return wrapper;
    }
}
