package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.sys.dao.RoleDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.command.AssignRoleUsersCommand;
import com.github.thundax.modules.sys.service.command.ChangeRoleInfoCommand;
import com.github.thundax.modules.sys.service.command.ChangeRolePriorityCommand;
import com.github.thundax.modules.sys.service.command.ChangeRoleStatusCommand;
import com.github.thundax.modules.sys.service.command.CreateRoleCommand;
import com.github.thundax.modules.sys.service.command.DeleteRoleCommand;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleDao dao;

    private final PooledThreadLocal<Map<Long, List<Long>>> idUserIdsMapHandler = new PooledThreadLocal<>();

    private final PooledThreadLocal<Map<Long, List<Long>>> idMenuIdsMapHandler = new PooledThreadLocal<>();

    public RoleServiceImpl(RoleDao dao) {
        this.dao = dao;
    }

    public Role get(RoleQuery query) {
        if (query == null || query.getId() == null) {
            return null;
        }
        return dao.getById(query.getId());
    }

    public List<Role> list(RoleQuery query) {
        return dao.list(query == null ? null : statusValue(query.getStatus()));
    }

    public PageResult<Role> page(RoleQuery query, PageQuery page) {
        PageQuery normalizedPage = normalizePage(page);
        IPage<Role> dataPage = dao.page(
                query == null ? null : statusValue(query.getStatus()),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId create(CreateRoleCommand command) {
        Role role = toRole(command);
        role.setId(dao.insert(role));
        afterWrite(role);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(ChangeRoleInfoCommand command) {
        Role role = toRole(command);
        dao.update(role);
        afterWrite(role);
    }

    private void afterWrite(Role role) {
        dao.deleteRoleMenu(EntityIdCodec.toValue(role.getId()));
        if (role.getMenuIdList() != null && !role.getMenuIdList().isEmpty()) {
            dao.insertRoleMenu(EntityIdCodec.toValue(role.getId()), role.getMenuIdList());
        }

        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(AssignRoleUsersCommand command) {
        dao.deleteRoleUser(EntityIdCodec.toValue(command.getRoleId()));

        if (command.getUserIds() != null && !command.getUserIds().isEmpty()) {
            dao.insertRoleUser(
                    EntityIdCodec.toValue(command.getRoleId()),
                    command.getUserIds().stream().map(EntityIdCodec::toValue).collect(Collectors.toList()));
        }

        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(ChangeRoleStatusCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setStatus(command.getStatus());
        int result = dao.updateStatus(role);

        notifyCacheChanged();

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public int remove(DeleteRoleCommand command) {
        EntityId id = command.getId();
        RoleQuery query = new RoleQuery();
        query.setId(id);
        Role role = get(query);
        if (role == null) {
            return 0;
        }

        dao.deleteRoleMenu(EntityIdCodec.toValue(id));
        dao.deleteRoleUser(EntityIdCodec.toValue(id));
        int retVal = dao.deleteById(id);

        notifyCacheChanged();

        return retVal;
    }

    @Override
    public List<User> listRoleUsers(RoleQuery query) {
        List<Long> userIdList = idUserIdsMapHandler
                .computeIfAbsent(HashMap::new)
                .computeIfAbsent(
                        EntityIdCodec.toValue(query.getId()),
                        roleId -> dao.listRoleUsers(EntityIdCodec.toValue(query.getId())));

        return userIdList.stream().map(this::newUser).collect(Collectors.toList());
    }

    @Override
    public List<Menu> listRoleMenus(RoleQuery query) {
        List<Long> menuIdList = idMenuIdsMapHandler
                .computeIfAbsent(HashMap::new)
                .computeIfAbsent(
                        EntityIdCodec.toValue(query.getId()),
                        roleId -> dao.listRoleMenus(EntityIdCodec.toValue(query.getId())));

        return menuIdList.stream().map(this::newMenu).collect(Collectors.toList());
    }

    private User newUser(Long id) {
        User user = new User();
        user.setId(EntityIdCodec.toDomain(id));
        return user;
    }

    private Menu newMenu(Long id) {
        Menu menu = new Menu();
        menu.setId(EntityIdCodec.toDomain(id));
        return menu;
    }

    private void notifyCacheChanged() {
        try {
            SpringContextHolder.getBeansOfType(CacheChangedListener.class)
                    .forEach((name, listener) -> listener.onRoleCacheChanged());
        } catch (IllegalStateException | NullPointerException ignored) {
            // Unit tests may instantiate the service without a Spring application context.
        }
    }

    public interface CacheChangedListener {

        void onRoleCacheChanged();
    }

    private String statusValue(RoleStatus status) {
        return status == null ? null : status.value();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changePriority(ChangeRolePriorityCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setPriority(command.getPriority());
        return dao.updatePriority(role);
    }

    private Role toRole(CreateRoleCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setName(command.getName());
        role.setPrivilege(command.getPrivilege());
        role.setStatus(command.getStatus());
        role.setPriority(command.getPriority());
        role.setRemarks(command.getRemarks());
        role.setMenuIdList(command.getMenuIdList());
        return role;
    }

    private Role toRole(ChangeRoleInfoCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setName(command.getName());
        role.setPrivilege(command.getPrivilege());
        role.setStatus(command.getStatus());
        role.setPriority(command.getPriority());
        role.setRemarks(command.getRemarks());
        role.setMenuIdList(command.getMenuIdList());
        return role;
    }

    private PageQuery normalizePage(PageQuery page) {
        PageQuery normalizedPage = page == null ? new PageQuery() : page;
        if (normalizedPage.getPageNo() < PageRules.firstPageIndex()) {
            normalizedPage.setPageNo(PageRules.firstPageIndex());
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(PageRules.defaultPageSize());
        }
        return normalizedPage;
    }
}
