package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.persistence.assembler.UserPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.cache.RoleCacheSupport;
import com.github.thundax.modules.sys.persistence.cache.UserCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import com.github.thundax.modules.sys.persistence.dataobject.UserRoleDO;
import com.github.thundax.modules.sys.persistence.mapper.UserMapper;
import com.github.thundax.modules.sys.persistence.mapper.UserRoleMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl implements UserDao {

    private static final String OFFICE_TREE_FILTER_SQL = "office_id IN (SELECT o.id FROM sys_office query_office "
            + "JOIN sys_office o ON o.lft BETWEEN query_office.lft AND query_office.rgt "
            + "WHERE query_office.id = {0})";
    private static final String DEL_FLAG_COLUMN = "del_flag";
    private static final String NORMAL_DEL_FLAG = "0";

    private final UserMapper mapper;
    private final UserRoleMapper userRoleMapper;
    private final UserCacheSupport cacheSupport;
    private final RoleCacheSupport roleCacheSupport;

    public UserDaoImpl(
            UserMapper mapper,
            UserRoleMapper userRoleMapper,
            UserCacheSupport cacheSupport,
            RoleCacheSupport roleCacheSupport) {
        this.mapper = mapper;
        this.userRoleMapper = userRoleMapper;
        this.cacheSupport = cacheSupport;
        this.roleCacheSupport = roleCacheSupport;
    }

    @Override
    public User get(String id) {
        User user = cacheSupport.getById(id);
        if (user != null) {
            return user;
        }
        user = UserPersistenceAssembler.toEntity(mapper.selectById(id));
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
            List<User> uncachedUserList = UserPersistenceAssembler.toEntityList(mapper.selectBatchIds(uncachedIdList));
            for (User user : uncachedUserList) {
                cacheSupport.putById(user);
                userList.add(user);
            }
        }
        return userList;
    }

    @Override
    public List<User> findList(String officeId, String loginName, String name, String enableFlag, String superFlag) {
        return UserPersistenceAssembler.toEntityList(
                mapper.selectList(buildListWrapper(officeId, loginName, name, enableFlag, superFlag)));
    }

    @Override
    public Page<User> findPage(
            String officeId,
            String loginName,
            String name,
            String enableFlag,
            String superFlag,
            int pageNo,
            int pageSize) {
        Page<UserDO> dataObjectPage = mapper.selectPage(
                new Page<>(pageNo, pageSize), buildListWrapper(officeId, loginName, name, enableFlag, superFlag));
        Page<User> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(UserPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public String insert(User entity) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);
        mapper.insert(dataObject);
        mapper.update(
                null, new UpdateWrapper<UserDO>().set(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG).eq("id", dataObject.getId()));
        removeUserCaches(dataObject.getId());
        return dataObject.getId();
    }

    @Override
    public int update(User entity) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(UserDO::getName, dataObject.getName())
                        .set(UserDO::getOfficeId, dataObject.getOfficeId())
                        .set(UserDO::getLoginName, dataObject.getLoginName())
                        .set(UserDO::getEmail, dataObject.getEmail())
                        .set(UserDO::getMobile, dataObject.getMobile())
                        .set(UserDO::getTel, dataObject.getTel())
                        .set(UserDO::getRanks, dataObject.getRanks())
                        .set(UserDO::getAdminFlag, dataObject.getAdminFlag())
                        .set(UserDO::getEnableFlag, dataObject.getEnableFlag())
                        .set(UserDO::getPriority, dataObject.getPriority())
                        .set(UserDO::getRemarks, dataObject.getRemarks())
                        .set(UserDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(UserDO::getUpdateBy, dataObject.getUpdateBy())
                        .set(UserDO::getSsoLoginName, dataObject.getSsoLoginName()));
        removeUserCaches(entity.getId());
        return count;
    }

    @Override
    public int updatePriority(User entity) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(UserDO::getPriority, dataObject.getPriority()));
        removeUserCaches(entity.getId());
        return count;
    }
    @Override
    public int delete(String id) {
        int count = mapper.deleteById(id);
        removeUserCaches(id);
        roleCacheSupport.removeAll();
        return count;
    }

    @Override
    public User getByLoginName(String loginName) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getLoginName, loginName);
        User user = UserPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
        cacheSupport.putById(user);
        return user;
    }

    @Override
    public User getBySsoLoginName(String ssoLoginName) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getSsoLoginName, ssoLoginName);
        User user = UserPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
        cacheSupport.putById(user);
        return user;
    }

    @Override
    public void updateLoginInfo(User user) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(user);
        mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(UserDO::getLastLoginDate, dataObject.getLastLoginDate())
                        .set(UserDO::getLastLoginIp, dataObject.getLastLoginIp())
                        .set(UserDO::getLoginCount, dataObject.getLoginCount()));
        removeUserCaches(user.getId());
    }

    @Override
    public int updateEnableFlag(User user) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(user);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(UserDO::getEnableFlag, dataObject.getEnableFlag())
                        .set(UserDO::getLoginCount, 0)
                        .set(UserDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(UserDO::getUpdateBy, dataObject.getUpdateBy()));
        removeUserCaches(user.getId());
        return count;
    }

    @Override
    public void updateLoginPass(User user) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(user);
        mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(UserDO::getLoginPass, dataObject.getLoginPass())
                        .set(UserDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(UserDO::getUpdateBy, dataObject.getUpdateBy()));
        removeUserCaches(user.getId());
    }

    @Override
    public List<String> findUserRole(String userId) {
        List<String> roleIds = cacheSupport.getUserRoleIds(userId);
        if (roleIds == null) {
            LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRoleDO::getUserId, userId);
            roleIds = userRoleMapper.selectList(wrapper).stream()
                    .map(UserRoleDO::getRoleId)
                    .collect(Collectors.toList());
            cacheSupport.putUserRoleIds(userId, roleIds);
        }
        return roleIds;
    }

    @Override
    public void deleteUserRole(String userId) {
        LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRoleDO::getUserId, userId);
        userRoleMapper.delete(wrapper);
        removeUserCaches(userId);
    }

    @Override
    public void insertUserRole(String userId, List<String> roleIdList) {
        for (String roleId : roleIdList) {
            userRoleMapper.insert(new UserRoleDO(userId, roleId));
        }
        removeUserCaches(userId);
    }

    private LambdaUpdateWrapper<UserDO> buildIdUpdateWrapper(UserDO dataObject) {
        LambdaUpdateWrapper<UserDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserDO::getId, dataObject.getId());
        return wrapper;
    }

    private QueryWrapper<UserDO> buildListWrapper(
            String officeId, String loginName, String name, String enableFlag, String superFlag) {
        QueryWrapper<UserDO> wrapper = new QueryWrapper<>();
        wrapper.eq(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG);
        if (StringUtils.isNotBlank(officeId)) {
            wrapper.apply(OFFICE_TREE_FILTER_SQL, officeId);
        }
        if (StringUtils.isNotBlank(loginName)) {
            wrapper.like("login_name", loginName);
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.like("name", name);
        }
        if (StringUtils.isNotBlank(enableFlag)) {
            wrapper.eq("enable_flag", enableFlag);
        }
        if (StringUtils.isNotBlank(superFlag)) {
            wrapper.eq("super_flag", superFlag);
        }
        wrapper.orderByAsc("priority", "create_date");
        return wrapper;
    }

    private void removeUserCaches(String userId) {
        cacheSupport.removeById(userId);
        cacheSupport.removeUserRoleIds(userId);
    }
}
