package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
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

    private static final String DEPARTMENT_TREE_FILTER_SQL =
            "department_id IN (SELECT o.id FROM sys_department query_department "
                    + "JOIN sys_department o ON o.lft BETWEEN query_department.lft AND query_department.rgt "
                    + "WHERE query_department.id = {0})";
    private static final String ACCOUNT_LOGIN_NAME_FILTER_SQL = "id IN (SELECT principal_id "
            + "FROM auth_principal_identity WHERE principal_type = 'USER' AND identity_type = 'USER_ACCOUNT' "
            + "AND identity_value LIKE CONCAT('%',{0},'%'))";
    private static final String DEL_FLAG_COLUMN = "del_flag";
    private static final String NORMAL_DEL_FLAG = "0";

    private final UserMapper mapper;
    private final UserRoleMapper userRoleMapper;
    private final UserCacheSupport cacheSupport;
    private final RoleCacheSupport roleCacheSupport;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

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
    public User getById(EntityId id) {
        User user = cacheSupport.getById(id.value());
        if (user != null) {
            return user;
        }
        user = UserPersistenceAssembler.toEntity(mapper.selectById(id.value()));
        cacheSupport.putById(user);
        return user;
    }

    @Override
    public List<User> listByIds(List<Long> idList) {
        List<User> userList = new ArrayList<>();
        List<Long> uncachedIdList = new ArrayList<>();
        for (Long id : idList) {
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
    public List<User> list(Long departmentId, String loginName, String name, String enableFlag, String superFlag) {
        return UserPersistenceAssembler.toEntityList(
                mapper.selectList(buildListWrapper(departmentId, loginName, name, enableFlag, superFlag)));
    }

    @Override
    public Page<User> page(
            Long departmentId,
            String loginName,
            String name,
            String enableFlag,
            String superFlag,
            int pageNo,
            int pageSize) {
        Page<UserDO> dataObjectPage = mapper.selectPage(
                new Page<>(pageNo, pageSize), buildListWrapper(departmentId, loginName, name, enableFlag, superFlag));
        Page<User> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(UserPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public EntityId insert(User entity) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        mapper.update(
                null,
                new UpdateWrapper<UserDO>()
                        .set(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG)
                        .eq("id", dataObject.getId()));
        removeUserCaches(dataObject.getId());
        return EntityIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(User entity) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(UserDO::getName, dataObject.getName())
                        .set(UserDO::getDepartmentId, dataObject.getDepartmentId())
                        .set(UserDO::getEmail, dataObject.getEmail())
                        .set(UserDO::getMobile, dataObject.getMobile())
                        .set(UserDO::getTel, dataObject.getTel())
                        .set(UserDO::getRanks, dataObject.getRanks())
                        .set(UserDO::getAdminFlag, dataObject.getAdminFlag())
                        .set(UserDO::getEnableFlag, dataObject.getEnableFlag())
                        .set(UserDO::getPriority, dataObject.getPriority())
                        .set(UserDO::getRemarks, dataObject.getRemarks()));
        removeUserCaches(EntityIdCodec.toValue(entity.getId()));
        return count;
    }

    @Override
    public int updatePriority(User entity) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(UserDO::getPriority, dataObject.getPriority()));
        removeUserCaches(EntityIdCodec.toValue(entity.getId()));
        return count;
    }

    @Override
    public int deleteById(EntityId id) {
        int count = mapper.deleteById(id.value());
        removeUserCaches(id.value());
        roleCacheSupport.removeAll();
        return count;
    }

    @Override
    public int updateStatus(User user) {
        UserDO dataObject = UserPersistenceAssembler.toDataObject(user);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(UserDO::getEnableFlag, dataObject.getEnableFlag()));
        removeUserCaches(EntityIdCodec.toValue(user.getId()));
        return count;
    }

    @Override
    public List<Long> listUserRoles(Long userId) {
        List<Long> roleIds = cacheSupport.getUserRoleIds(userId);
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
    public void deleteUserRole(Long userId) {
        LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRoleDO::getUserId, userId);
        userRoleMapper.delete(wrapper);
        removeUserCaches(userId);
    }

    @Override
    public void insertUserRole(Long userId, List<Long> roleIdList) {
        for (Long roleId : roleIdList) {
            userRoleMapper.insert(UserPersistenceAssembler.toUserRoleDataObject(userId, roleId));
        }
        removeUserCaches(userId);
    }

    private LambdaUpdateWrapper<UserDO> buildIdUpdateWrapper(UserDO dataObject) {
        LambdaUpdateWrapper<UserDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserDO::getId, dataObject.getId());
        return wrapper;
    }

    private QueryWrapper<UserDO> buildListWrapper(
            Long departmentId, String loginName, String name, String enableFlag, String superFlag) {
        QueryWrapper<UserDO> wrapper = new QueryWrapper<>();
        wrapper.eq(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG);
        if (departmentId != null) {
            wrapper.apply(DEPARTMENT_TREE_FILTER_SQL, departmentId);
        }
        if (StringUtils.isNotBlank(loginName)) {
            wrapper.apply(ACCOUNT_LOGIN_NAME_FILTER_SQL, loginName);
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

    private void removeUserCaches(Long userId) {
        cacheSupport.removeById(userId);
        cacheSupport.removeUserRoleIds(userId);
    }
}
