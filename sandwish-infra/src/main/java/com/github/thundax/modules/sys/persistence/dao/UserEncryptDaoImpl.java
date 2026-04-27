package com.github.thundax.modules.sys.persistence.dao;

import com.github.thundax.modules.sys.dao.UserEncryptDao;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.persistence.assembler.UserEncryptPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.UserEncryptDO;
import com.github.thundax.modules.sys.persistence.mapper.UserEncryptMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户加密信息 DAO 实现。
 */
@Repository
public class UserEncryptDaoImpl implements UserEncryptDao {

    private final UserEncryptMapper mapper;

    public UserEncryptDaoImpl(UserEncryptMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserEncrypt get(UserEncrypt entity) {
        return UserEncryptPersistenceAssembler.toEntity(
                mapper.get(UserEncryptPersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<UserEncrypt> getMany(List<String> idList) {
        return UserEncryptPersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<UserEncrypt> findList(UserEncrypt entity) {
        List<UserEncryptDO> dataObjects = mapper.findList(UserEncryptPersistenceAssembler.toDataObject(entity));
        List<UserEncrypt> entities = UserEncryptPersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(UserEncrypt entity) {
        return mapper.insert(UserEncryptPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(UserEncrypt entity) {
        return mapper.update(UserEncryptPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(UserEncrypt entity) {
        return 0;
    }

    public int updateStatus(UserEncrypt entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(UserEncrypt entity) {
        return 0;
    }

    @Override
    public int delete(UserEncrypt entity) {
        return mapper.delete(UserEncryptPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public void updateLoginPass(UserEncrypt userEncrypt) {
        mapper.updateLoginPass(UserEncryptPersistenceAssembler.toDataObject(userEncrypt));
    }
}
