package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.RoleDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.RoleStatus;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.RoleService;
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
    public Role get(Role entity) {
        return entity == null ? null : get(entity.getId());
    }

    @Override
    public Role get(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.get(id);
    }

    @Override
    public List<Role> getMany(List<String> ids) {
        return dao.getMany(ids);
    }

    @Override
    public List<Role> findList(Role role) {
        Role.Query query = role == null ? null : role.getQuery();
        return dao.findList(query == null ? null : statusValue(query.getStatus()));
    }

    @Override
    public Role findOne(Role role) {
        List<Role> roles = findList(role);
        return roles == null || roles.isEmpty() ? null : roles.get(0);
    }

    @Override
    public Page<Role> findPage(Role role, Page<Role> page) {
        Page<Role> normalizedPage = normalizePage(page);
        Role.Query query = role == null ? null : role.getQuery();
        IPage<Role> dataPage = dao.findPage(
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
        List<Role> roles = findList(role);
        return roles == null ? 0 : roles.size();
    }

    @Override
    public List<Role> findValidList() {
        Role query = new Role();
        Role.Query queryCondition = new Role.Query();
        queryCondition.setStatus(RoleStatus.ENABLED);
        query.setQuery(queryCondition);
        return this.findList(query);
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
    public int updateEnableFlag(Role role) {
        int result = dao.updateEnableFlag(role);

        signService.sign(role.getSignName(), role.getSignId(), role.getSignBody());
        notifyCacheChanged();

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(List<Role> list) {
        return batchOperate(list, this::updateEnableFlag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Role role) {
        dao.deleteRoleMenu(EntityIdCodec.toValue(role.getId()));
        dao.deleteRoleUser(EntityIdCodec.toValue(role.getId()));
        int retVal = dao.delete(role.getId());

        signService.deleteSign(role.getSignName(), role.getSignId());
        notifyCacheChanged();

        return retVal;
    }

    @Override
    public List<User> findRoleUser(Role role) {
        List<String> userIdList = idUserIdsMapHandler
                .computeIfAbsent(HashMap::new)
                .computeIfAbsent(
                        EntityIdCodec.toValue(role.getId()),
                        roleId -> dao.findRoleUser(EntityIdCodec.toValue(role.getId())));

        return userIdList.stream().map(this::newUser).collect(Collectors.toList());
    }

    @Override
    public List<Menu> findRoleMenu(Role role) {
        List<String> menuIdList = idMenuIdsMapHandler
                .computeIfAbsent(HashMap::new)
                .computeIfAbsent(
                        EntityIdCodec.toValue(role.getId()),
                        roleId -> dao.findRoleMenu(EntityIdCodec.toValue(role.getId())));

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
    public int delete(List<Role> list) {
        return batchOperate(list, this::delete);
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

    private int batchOperate(Collection<Role> collection, Function<Role, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (Role role : collection) {
                count += operator.apply(role);
            }
        }
        return count;
    }

    private Page<Role> normalizePage(Page<Role> page) {
        Page<Role> normalizedPage = page == null ? new Page<>() : page;
        if (normalizedPage.getPageNo() < Page.FIRST_PAGE_INDEX) {
            normalizedPage.setPageNo(Page.FIRST_PAGE_INDEX);
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(Page.DEFAULT_PAGE_SIZE);
        }
        return normalizedPage;
    }
}
