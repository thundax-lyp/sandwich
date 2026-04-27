package com.github.thundax.modules.sys.persistence.dao;

import com.github.pagehelper.Page;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.persistence.assembler.UserPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.cache.RoleCacheSupport;
import com.github.thundax.modules.sys.persistence.cache.UserCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import com.github.thundax.modules.sys.persistence.mapper.UserMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/** 用户 DAO 实现。 */
@Repository
public class UserDaoImpl implements UserDao {

    private final UserMapper mapper;
    private final UserCacheSupport cacheSupport;
    private final RoleCacheSupport roleCacheSupport;

    public UserDaoImpl(
            UserMapper mapper, UserCacheSupport cacheSupport, RoleCacheSupport roleCacheSupport) {
        this.mapper = mapper;
        this.cacheSupport = cacheSupport;
        this.roleCacheSupport = roleCacheSupport;
    }

    @Override
    public User get(User entity) {
        User user = cacheSupport.getById(entity.getId());
        if (user != null) {
            return user;
        }

        user =
                UserPersistenceAssembler.toEntity(
                        mapper.get(UserPersistenceAssembler.toDataObject(entity)));
        cacheSupport.putById(user);
        return user;
    }

    @Override
    public List<User> getMany(List<String> idList) {
        List<User> userList = new ArrayList<>();
        List<String> uncachedIdList = new ArrayList<>();
        for (String id : idList) {
            User user = cacheSupport.getById(id);
            if (user == null) {
                uncachedIdList.add(id);
            } else {
                userList.add(user);
            }
        }

        if (!uncachedIdList.isEmpty()) {
            List<User> uncachedUserList =
                    UserPersistenceAssembler.toEntityList(mapper.getMany(uncachedIdList));
            for (User user : uncachedUserList) {
                cacheSupport.putById(user);
                userList.add(user);
            }
        }
        return userList;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<User> findList(User entity) {
        List<UserDO> dataObjects = mapper.findList(UserPersistenceAssembler.toDataObject(entity));
        List<User> entities = UserPersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(User entity) {
        int count = mapper.insert(UserPersistenceAssembler.toDataObject(entity));
        removeUserCaches(entity);
        return count;
    }

    @Override
    public int update(User entity) {
        int count = mapper.update(UserPersistenceAssembler.toDataObject(entity));
        removeUserCaches(entity);
        return count;
    }

    @Override
    public int updatePriority(User entity) {
        int count = mapper.updatePriority(UserPersistenceAssembler.toDataObject(entity));
        removeUserCaches(entity);
        return count;
    }

    public int updateStatus(User entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(User entity) {
        removeUserCaches(entity);
        return 0;
    }

    @Override
    public int delete(User entity) {
        int count = mapper.delete(UserPersistenceAssembler.toDataObject(entity));
        removeUserCaches(entity);
        roleCacheSupport.removeAll();
        return count;
    }

    @Override
    public User getByLoginName(String loginName) {
        User user = UserPersistenceAssembler.toEntity(mapper.getByLoginName(loginName));
        cacheSupport.putById(user);
        return user;
    }

    @Override
    public User getBySsoLoginName(String ssoLoginName) {
        User user = UserPersistenceAssembler.toEntity(mapper.getBySsoLoginName(ssoLoginName));
        cacheSupport.putById(user);
        return user;
    }

    @Override
    public void updateLoginInfo(User user) {
        mapper.updateLoginInfo(UserPersistenceAssembler.toDataObject(user));
        removeUserCaches(user);
    }

    @Override
    public int updateEnableFlag(User user) {
        int count = mapper.updateEnableFlag(UserPersistenceAssembler.toDataObject(user));
        removeUserCaches(user);
        return count;
    }

    @Override
    public void updateLoginPass(User user) {
        mapper.updateLoginPass(UserPersistenceAssembler.toDataObject(user));
        removeUserCaches(user);
    }

    @Override
    public List<Role> findUserRole(User user) {
        List<String> roleIds = cacheSupport.getUserRoleIds(user.getId());
        if (roleIds == null) {
            roleIds =
                    UserPersistenceAssembler.toRoleList(
                                    mapper.findUserRole(
                                            UserPersistenceAssembler.toDataObject(user)))
                            .stream()
                            .map(role -> role.getId())
                            .collect(Collectors.toList());
            cacheSupport.putUserRoleIds(user.getId(), roleIds);
        }
        return roleIds.stream().map(roleId -> new Role(roleId)).collect(Collectors.toList());
    }

    @Override
    public void deleteUserRole(User user) {
        mapper.deleteUserRole(UserPersistenceAssembler.toDataObject(user));
        removeUserCaches(user);
    }

    @Override
    public void insertUserRole(User user) {
        mapper.insertUserRole(UserPersistenceAssembler.toDataObjectWithRoles(user));
        removeUserCaches(user);
    }

    private void removeUserCaches(User user) {
        cacheSupport.removeById(user.getId());
        cacheSupport.removeUserRoleIds(user.getId());
    }
}
