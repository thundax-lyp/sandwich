package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.sys.dao.RoleDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.persistence.assembler.RolePersistenceAssembler;
import com.github.thundax.modules.sys.persistence.cache.RoleCacheSupport;
import com.github.thundax.modules.sys.persistence.cache.UserCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.MenuRoleDO;
import com.github.thundax.modules.sys.persistence.dataobject.RoleDO;
import com.github.thundax.modules.sys.persistence.dataobject.UserRoleDO;
import com.github.thundax.modules.sys.persistence.mapper.MenuRoleMapper;
import com.github.thundax.modules.sys.persistence.mapper.RoleMapper;
import com.github.thundax.modules.sys.persistence.mapper.UserRoleMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDaoImpl implements RoleDao {

    private final RoleMapper mapper;
    private final MenuRoleMapper menuRoleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleCacheSupport cacheSupport;
    private final UserCacheSupport userCacheSupport;

    public RoleDaoImpl(
            RoleMapper mapper,
            MenuRoleMapper menuRoleMapper,
            UserRoleMapper userRoleMapper,
            RoleCacheSupport cacheSupport,
            UserCacheSupport userCacheSupport) {
        this.mapper = mapper;
        this.menuRoleMapper = menuRoleMapper;
        this.userRoleMapper = userRoleMapper;
        this.cacheSupport = cacheSupport;
        this.userCacheSupport = userCacheSupport;
    }

    @Override
    public Role get(String id) {
        Role role = cacheSupport.getById(id);
        if (role != null) {
            return role;
        }
        role = RolePersistenceAssembler.toEntity(mapper.selectById(id));
        cacheSupport.putById(role);
        return role;
    }

    @Override
    public List<Role> getMany(List<String> idList) {
        List<Role> roleList = new ArrayList<>();
        List<String> uncachedIdList = new ArrayList<>();
        for (String id : idList) {
            Role role = cacheSupport.getById(id);
            if (role == null) {
                uncachedIdList.add(id);
            } else {
                roleList.add(role);
            }
        }
        if (!uncachedIdList.isEmpty()) {
            List<Role> uncachedRoleList = RolePersistenceAssembler.toEntityList(mapper.selectBatchIds(uncachedIdList));
            for (Role role : uncachedRoleList) {
                cacheSupport.putById(role);
                roleList.add(role);
            }
        }
        return roleList;
    }

    @Override
    public List<Role> findList(String enableFlag) {
        return RolePersistenceAssembler.toEntityList(mapper.selectList(buildListWrapper(enableFlag)));
    }

    @Override
    public Page<Role> findPage(String enableFlag, int pageNo, int pageSize) {
        Page<RoleDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper(enableFlag));
        Page<Role> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(RolePersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public String insert(Role entity) {
        RoleDO dataObject = RolePersistenceAssembler.toDataObject(entity);
        mapper.insert(dataObject);
        cacheSupport.removeById(dataObject.getId());
        return dataObject.getId();
    }

    @Override
    public int update(Role entity) {
        RoleDO dataObject = RolePersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(RoleDO::getName, dataObject.getName())
                        .set(RoleDO::getAdminFlag, dataObject.getAdminFlag())
                        .set(RoleDO::getEnableFlag, dataObject.getEnableFlag())
                        .set(RoleDO::getPriority, dataObject.getPriority())
                        .set(RoleDO::getRemarks, dataObject.getRemarks())
                        .set(RoleDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(RoleDO::getUpdateUserId, dataObject.getUpdateUserId()));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int updatePriority(Role entity) {
        RoleDO dataObject = RolePersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(RoleDO::getPriority, dataObject.getPriority()));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int updateDelFlag(Role entity) {
        RoleDO dataObject = RolePersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(RoleDO::getDelFlag, dataObject.getDelFlag())
                        .set(RoleDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(RoleDO::getUpdateUserId, dataObject.getUpdateUserId()));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int delete(String id) {
        int count = mapper.deleteById(id);
        removeRoleCaches(id);
        return count;
    }

    @Override
    public int updateEnableFlag(Role role) {
        RoleDO dataObject = RolePersistenceAssembler.toDataObject(role);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(RoleDO::getEnableFlag, dataObject.getEnableFlag())
                        .set(RoleDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(RoleDO::getUpdateUserId, dataObject.getUpdateUserId()));
        cacheSupport.removeById(role.getId());
        return count;
    }

    @Override
    public List<String> findRoleMenu(String roleId) {
        List<String> menuIds = cacheSupport.getRoleMenuIds(roleId);
        if (menuIds == null) {
            LambdaQueryWrapper<MenuRoleDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MenuRoleDO::getRoleId, roleId);
            menuIds = menuRoleMapper.selectList(wrapper).stream()
                    .map(MenuRoleDO::getMenuId)
                    .collect(Collectors.toList());
            cacheSupport.putRoleMenuIds(roleId, menuIds);
        }
        return menuIds;
    }

    @Override
    public void deleteRoleMenu(String roleId) {
        LambdaQueryWrapper<MenuRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuRoleDO::getRoleId, roleId);
        menuRoleMapper.delete(wrapper);
        removeRoleCaches(roleId);
    }

    @Override
    public void insertRoleMenu(String roleId, List<String> menuIdList) {
        for (String menuId : menuIdList) {
            menuRoleMapper.insert(new MenuRoleDO(roleId, menuId));
        }
        removeRoleCaches(roleId);
    }

    @Override
    public List<String> findRoleUser(String roleId) {
        List<String> userIds = cacheSupport.getRoleUserIds(roleId);
        if (userIds == null) {
            LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRoleDO::getRoleId, roleId);
            userIds = userRoleMapper.selectList(wrapper).stream()
                    .map(UserRoleDO::getUserId)
                    .collect(Collectors.toList());
            cacheSupport.putRoleUserIds(roleId, userIds);
        }
        return userIds;
    }

    @Override
    public void deleteRoleUser(String roleId) {
        LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRoleDO::getRoleId, roleId);
        userRoleMapper.delete(wrapper);
        removeRoleCaches(roleId);
    }

    @Override
    public void insertRoleUser(String roleId, List<String> userIdList) {
        for (String userId : userIdList) {
            userRoleMapper.insert(new UserRoleDO(userId, roleId));
        }
        removeRoleCaches(roleId);
    }

    private LambdaUpdateWrapper<RoleDO> buildIdUpdateWrapper(RoleDO dataObject) {
        LambdaUpdateWrapper<RoleDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RoleDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaQueryWrapper<RoleDO> buildListWrapper(String enableFlag) {
        LambdaQueryWrapper<RoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleDO::getDelFlag, RoleDO.DEL_FLAG_NORMAL);
        if (StringUtils.isNotBlank(enableFlag)) {
            wrapper.eq(RoleDO::getEnableFlag, enableFlag);
        }
        wrapper.orderByAsc(RoleDO::getPriority, RoleDO::getCreateDate);
        return wrapper;
    }

    private void removeRoleCaches(String roleId) {
        cacheSupport.removeById(roleId);
        cacheSupport.removeRoleUserIds(roleId);
        cacheSupport.removeRoleMenuIds(roleId);
        userCacheSupport.removeAll();
    }
}
