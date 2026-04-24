package com.github.thundax.modules.sys.persistence.dao;

import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.persistence.assembler.UserPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import com.github.thundax.modules.sys.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户 DAO 实现。
 */
@Repository
public class UserDaoImpl implements UserDao {

    private final UserMapper mapper;

    public UserDaoImpl(UserMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public User get(User entity) {
        return UserPersistenceAssembler.toEntity(mapper.get(UserPersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<User> getMany(List<String> idList) {
        return UserPersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<User> findList(User entity) {
        List<UserDO> dataObjects = mapper.findList(UserPersistenceAssembler.toDataObject(entity));
        List<User> entities = UserPersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(User entity) {
        return mapper.insert(UserPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(User entity) {
        return mapper.update(UserPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(User entity) {
        return mapper.updatePriority(UserPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updateStatus(User entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(User entity) {
        return 0;
    }

    @Override
    public int delete(User entity) {
        return mapper.delete(UserPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public User getByLoginName(String loginName) {
        return UserPersistenceAssembler.toEntity(mapper.getByLoginName(loginName));
    }

    @Override
    public User getBySsoLoginName(String ssoLoginName) {
        return UserPersistenceAssembler.toEntity(mapper.getBySsoLoginName(ssoLoginName));
    }

    @Override
    public void updateLoginInfo(User user) {
        mapper.updateLoginInfo(UserPersistenceAssembler.toDataObject(user));
    }

    @Override
    public int updateEnableFlag(User user) {
        return mapper.updateEnableFlag(UserPersistenceAssembler.toDataObject(user));
    }

    @Override
    public void updateLoginPass(User user) {
        mapper.updateLoginPass(UserPersistenceAssembler.toDataObject(user));
    }

    @Override
    public List<Role> findUserRole(User user) {
        return UserPersistenceAssembler.toRoleList(mapper.findUserRole(UserPersistenceAssembler.toDataObject(user)));
    }

    @Override
    public void deleteUserRole(User user) {
        mapper.deleteUserRole(UserPersistenceAssembler.toDataObject(user));
    }

    @Override
    public void insertUserRole(User user) {
        mapper.insertUserRole(UserPersistenceAssembler.toDataObjectWithRoles(user));
    }
}
