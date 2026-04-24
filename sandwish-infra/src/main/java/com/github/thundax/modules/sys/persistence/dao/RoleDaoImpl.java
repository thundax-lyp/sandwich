package com.github.thundax.modules.sys.persistence.dao;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.modules.sys.dao.RoleDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.persistence.assembler.RolePersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.RoleDO;
import com.github.thundax.modules.sys.persistence.mapper.RoleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色 DAO 实现。
 */
@Repository
public class RoleDaoImpl implements RoleDao {

    private final RoleMapper mapper;

    public RoleDaoImpl(RoleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Role get(Role entity) {
        return RolePersistenceAssembler.toEntity(mapper.get(RolePersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<Role> getMany(List<String> idList) {
        return RolePersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Role> findList(Role entity) {
        List<RoleDO> dataObjects = mapper.findList(RolePersistenceAssembler.toDataObject(entity));
        List<Role> entities = RolePersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Role entity) {
        return mapper.insert(RolePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(Role entity) {
        return mapper.update(RolePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(Role entity) {
        return mapper.updatePriority(RolePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updateStatus(Role entity) {
        return mapper.updateStatus(RolePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updateDelFlag(Role entity) {
        return mapper.updateDelFlag(RolePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int delete(Role entity) {
        return mapper.delete(RolePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updateEnableFlag(Role role) {
        return mapper.updateEnableFlag(RolePersistenceAssembler.toDataObject(role));
    }

    @Override
    public List<Menu> findRoleMenu(Role role) {
        return ListUtils.map(mapper.findRoleMenu(RolePersistenceAssembler.toDataObject(role)), Menu::new);
    }

    @Override
    public void deleteRoleMenu(Role role) {
        mapper.deleteRoleMenu(RolePersistenceAssembler.toDataObject(role));
    }

    @Override
    public void insertRoleMenu(Role role) {
        mapper.insertRoleMenu(RolePersistenceAssembler.toDataObjectWithMenus(role));
    }

    @Override
    public List<User> findRoleUser(Role role) {
        return RolePersistenceAssembler.toUserList(mapper.findRoleUser(RolePersistenceAssembler.toDataObject(role)));
    }

    @Override
    public void deleteRoleUser(Role role) {
        mapper.deleteRoleUser(RolePersistenceAssembler.toDataObject(role));
    }

    @Override
    public void insertRoleUser(Role role, List<User> userList) {
        mapper.insertRoleUser(RolePersistenceAssembler.toDataObject(role),
                RolePersistenceAssembler.toUserIdList(userList));
    }
}
