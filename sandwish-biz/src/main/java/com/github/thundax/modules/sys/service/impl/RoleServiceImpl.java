package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.persistence.PageRules;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.RoleDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleDao dao;
    private final SignService signService;

    private final PooledThreadLocal<Map<String, List<String>>> idUserIdsMapHandler = new PooledThreadLocal<>();

    private final PooledThreadLocal<Map<String, List<String>>> idMenuIdsMapHandler = new PooledThreadLocal<>();

    public RoleServiceImpl(RoleDao dao, SignService signService) {
        this.dao = dao;
        this.signService = signService;
    }

    @Override
    public Class<Role> getElementType() {
        return Role.class;
    }

    @Override
    public Role newEntity(String id) {
        Role role = new Role();
        role.setId(EntityIdCodec.toDomain(id));
        return role;
    }

    @Override
    public Role getById(Role entity) {
        return entity == null ? null : getById(entity.getId());
    }

    @Override
    public Role getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Role> batchGetByIds(List<EntityId> ids) {
        return dao.batchGetByIds(EntityIdCodec.toValues(ids));
    }

    @Override
    public List<Role> list(Role role) {
        return list((RoleQuery) null);
    }

    @Override
    public List<Role> list(RoleQuery query) {
        return dao.list(query == null ? null : statusValue(query.getStatus()));
    }

    @Override
    public Role getOne(Role role) {
        List<Role> roles = list(role);
        return roles == null || roles.isEmpty() ? null : roles.get(0);
    }

    @Override
    public Page<Role> page(Role role, Page<Role> page) {
        return page((RoleQuery) null, page);
    }

    @Override
    public Page<Role> page(RoleQuery query, Page<Role> page) {
        Page<Role> normalizedPage = normalizePage(page);
        IPage<Role> dataPage = dao.page(
                query == null ? null : statusValue(query.getStatus()),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    public long count(Role role) {
        List<Role> roles = list(role);
        return roles == null ? 0 : roles.size();
    }

    @Override
    public List<Role> listEnabled() {
        RoleQuery query = new RoleQuery();
        query.setStatus(RoleStatus.ENABLED);
        return this.list(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Role role) {
        role.setId(EntityIdCodec.toDomain(dao.insert(role)));
        afterWrite(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Role role) {
        dao.update(role);
        afterWrite(role);
    }

    private void afterWrite(Role role) {
        dao.deleteRoleMenu(EntityIdCodec.toValue(role.getId()));
        if (role.getMenuIdList() != null && !role.getMenuIdList().isEmpty()) {
            dao.insertRoleMenu(EntityIdCodec.toValue(role.getId()), role.getMenuIdList());
        }

        signService.sign(role.getSignName(), role.getSignId(), role.getSignBody());
        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserList(Role role, List<User> userList) {
        dao.deleteRoleUser(EntityIdCodec.toValue(role.getId()));

        if (userList != null && !userList.isEmpty()) {
            dao.insertRoleUser(
                    EntityIdCodec.toValue(role.getId()),
                    userList.stream()
                            .map(user -> EntityIdCodec.toValue(user.getId()))
                            .collect(Collectors.toList()));
        }

        signService.sign(role.getSignName(), role.getSignId(), role.getSignBody());
        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(Role role) {
        int result = dao.updateStatus(role);

        signService.sign(role.getSignName(), role.getSignId(), role.getSignBody());
        notifyCacheChanged();

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(List<Role> list) {
        return batchOperate(list, this::updateStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        Role role = getById(id);
        if (role == null) {
            return 0;
        }

        dao.deleteRoleMenu(EntityIdCodec.toValue(id));
        dao.deleteRoleUser(EntityIdCodec.toValue(id));
        int retVal = dao.deleteById(id);

        signService.deleteSign(role.getSignName(), role.getSignId());
        notifyCacheChanged();

        return retVal;
    }

    @Override
    public List<User> listRoleUsers(Role role) {
        List<String> userIdList = idUserIdsMapHandler
                .computeIfAbsent(HashMap::new)
                .computeIfAbsent(
                        EntityIdCodec.toValue(role.getId()),
                        roleId -> dao.listRoleUsers(EntityIdCodec.toValue(role.getId())));

        return userIdList.stream().map(this::newUser).collect(Collectors.toList());
    }

    @Override
    public List<Menu> listRoleMenus(Role role) {
        List<String> menuIdList = idMenuIdsMapHandler
                .computeIfAbsent(HashMap::new)
                .computeIfAbsent(
                        EntityIdCodec.toValue(role.getId()),
                        roleId -> dao.listRoleMenus(EntityIdCodec.toValue(role.getId())));

        return menuIdList.stream().map(this::newMenu).collect(Collectors.toList());
    }

    private User newUser(String id) {
        User user = new User();
        user.setId(EntityIdCodec.toDomain(id));
        return user;
    }

    private Menu newMenu(String id) {
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
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(Role role) {
        return dao.updatePriority(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<Role> list) {
        return batchOperate(list, this::updatePriority);
    }

    private <T> int batchOperate(Collection<T> collection, Function<T, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (T entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private Page<Role> normalizePage(Page<Role> page) {
        Page<Role> normalizedPage = page == null ? new Page<>() : page;
        if (normalizedPage.getPageNo() < PageRules.firstPageIndex()) {
            normalizedPage.setPageNo(PageRules.firstPageIndex());
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(PageRules.defaultPageSize());
        }
        return normalizedPage;
    }
}
