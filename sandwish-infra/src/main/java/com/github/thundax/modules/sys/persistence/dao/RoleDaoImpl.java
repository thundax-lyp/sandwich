package com.github.thundax.modules.sys.persistence.dao;

import com.github.pagehelper.Page;
import com.github.thundax.modules.sys.dao.RoleDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.persistence.assembler.RolePersistenceAssembler;
import com.github.thundax.modules.sys.persistence.cache.RoleCacheSupport;
import com.github.thundax.modules.sys.persistence.cache.UserCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.RoleDO;
import com.github.thundax.modules.sys.persistence.mapper.RoleMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/** 角色 DAO 实现。 */
@Repository
public class RoleDaoImpl implements RoleDao {

    private final RoleMapper mapper;
    private final RoleCacheSupport cacheSupport;
    private final UserCacheSupport userCacheSupport;

    public RoleDaoImpl(
            RoleMapper mapper, RoleCacheSupport cacheSupport, UserCacheSupport userCacheSupport) {
        this.mapper = mapper;
        this.cacheSupport = cacheSupport;
        this.userCacheSupport = userCacheSupport;
    }

    @Override
    public Role get(Role entity) {
        Role role = cacheSupport.getById(entity.getId());
        if (role != null) {
            return role;
        }

        role =
                RolePersistenceAssembler.toEntity(
                        mapper.get(RolePersistenceAssembler.toDataObject(entity)));
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
            List<Role> uncachedRoleList =
                    RolePersistenceAssembler.toEntityList(mapper.getMany(uncachedIdList));
            for (Role role : uncachedRoleList) {
                cacheSupport.putById(role);
                roleList.add(role);
            }
        }
        return roleList;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Role> findList(Role entity) {
        List<RoleDO> dataObjects = mapper.findList(RolePersistenceAssembler.toDataObject(entity));
        List<Role> entities = RolePersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Role entity) {
        int count = mapper.insert(RolePersistenceAssembler.toDataObject(entity));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int update(Role entity) {
        int count = mapper.update(RolePersistenceAssembler.toDataObject(entity));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int updatePriority(Role entity) {
        int count = mapper.updatePriority(RolePersistenceAssembler.toDataObject(entity));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    public int updateStatus(Role entity) {
        return mapper.updateStatus(RolePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updateDelFlag(Role entity) {
        int count = mapper.updateDelFlag(RolePersistenceAssembler.toDataObject(entity));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int delete(Role entity) {
        int count = mapper.delete(RolePersistenceAssembler.toDataObject(entity));
        removeRoleCaches(entity);
        return count;
    }

    @Override
    public int updateEnableFlag(Role role) {
        int count = mapper.updateEnableFlag(RolePersistenceAssembler.toDataObject(role));
        cacheSupport.removeById(role.getId());
        return count;
    }

    @Override
    public List<Menu> findRoleMenu(Role role) {
        List<String> menuIds = cacheSupport.getRoleMenuIds(role.getId());
        if (menuIds == null) {
            menuIds = mapper.findRoleMenu(RolePersistenceAssembler.toDataObject(role));
            cacheSupport.putRoleMenuIds(role.getId(), menuIds);
        }
        return menuIds.stream().map(menuId -> new Menu(menuId)).collect(Collectors.toList());
    }

    @Override
    public void deleteRoleMenu(Role role) {
        mapper.deleteRoleMenu(RolePersistenceAssembler.toDataObject(role));
        removeRoleCaches(role);
    }

    @Override
    public void insertRoleMenu(Role role) {
        mapper.insertRoleMenu(RolePersistenceAssembler.toDataObjectWithMenus(role));
        removeRoleCaches(role);
    }

    @Override
    public List<User> findRoleUser(Role role) {
        List<String> userIds = cacheSupport.getRoleUserIds(role.getId());
        if (userIds == null) {
            userIds = mapper.findRoleUser(RolePersistenceAssembler.toDataObject(role));
            cacheSupport.putRoleUserIds(role.getId(), userIds);
        }
        return userIds.stream().map(userId -> new User(userId)).collect(Collectors.toList());
    }

    @Override
    public void deleteRoleUser(Role role) {
        mapper.deleteRoleUser(RolePersistenceAssembler.toDataObject(role));
        removeRoleCaches(role);
    }

    @Override
    public void insertRoleUser(Role role, List<User> userList) {
        mapper.insertRoleUser(
                RolePersistenceAssembler.toDataObject(role),
                RolePersistenceAssembler.toUserIdList(userList));
        removeRoleCaches(role);
    }

    private void removeRoleCaches(Role role) {
        cacheSupport.removeById(role.getId());
        cacheSupport.removeRoleUserIds(role.getId());
        cacheSupport.removeRoleMenuIds(role.getId());
        userCacheSupport.removeAll();
    }
}
