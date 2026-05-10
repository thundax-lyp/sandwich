package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.ErrorCode;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.sys.dao.RoleDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.entity.valueobject.MenuIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.RoleId;
import com.github.thundax.modules.sys.entity.valueobject.RoleIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.command.AssignRoleUsersCommand;
import com.github.thundax.modules.sys.service.command.ChangeRoleInfoCommand;
import com.github.thundax.modules.sys.service.command.ChangeRoleStatusCommand;
import com.github.thundax.modules.sys.service.command.CreateRoleCommand;
import com.github.thundax.modules.sys.service.command.DeleteRoleCommand;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private static final int PRIORITY_STEP = 10;

    private final RoleDao dao;

    private final PooledThreadLocal<Map<Long, List<Long>>> idUserIdsMapHandler = new PooledThreadLocal<>();

    private final PooledThreadLocal<Map<Long, List<Long>>> idMenuIdsMapHandler = new PooledThreadLocal<>();

    public RoleServiceImpl(RoleDao dao) {
        this.dao = dao;
    }

    public Role get(RoleId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
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
    @AuditLog(type = "Role", id = "", action = AuditAction.CREATE, summary = "创建角色", recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public RoleId create(CreateRoleCommand command) {
        Role role = toRole(command);
        role.setPriority(dao.maxPriority() + PRIORITY_STEP);
        role.setId(dao.insert(role));
        afterWrite(role);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(List<RoleId> orderedIds, SortDirection sortDirection) throws ApiException {
        SortDirection effectiveDirection = sortDirection == null ? SortDirection.ASC : sortDirection;
        List<RoleId> orderedIdList = normalizeOrderedIds(orderedIds);
        if (orderedIdList.isEmpty()) {
            throw new ApiException(ErrorCode.SORT_EMPTY_INPUT.getCode(), ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        List<Role> selectedRoles = dao.listByIds(toValues(orderedIdList));
        if (selectedRoles == null || selectedRoles.isEmpty()) {
            throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
        }

        for (Role role : selectedRoles) {
            if (role == null || role.getId() == null) {
                throw new ApiException(ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessage());
            }
        }

        for (RoleId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null) {
                throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
            }
        }

        List<Role> currentRoles = dao.list(effectiveDirection);
        if (currentRoles == null || currentRoles.isEmpty()) {
            throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
        }
        if (currentRoles.size() != orderedIdList.size()) {
            throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
        }

        Map<Long, Integer> indexById = new HashMap<>(currentRoles.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentRoles.size());
        List<RoleId> currentOrderedIds = new ArrayList<>(currentRoles.size());
        for (int i = 0; i < currentRoles.size(); i++) {
            Role role = currentRoles.get(i);
            if (role == null || role.getId() == null) {
                throw new ApiException(ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessage());
            }
            long roleId = role.getId().value();
            indexById.put(roleId, i);
            priorityById.put(roleId, role.getPriority());
            currentOrderedIds.add(role.getId());
        }

        for (RoleId orderedId : orderedIdList) {
            if (!indexById.containsKey(orderedId.value())) {
                throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
            }
        }

        try {
            int temporaryPriority = dao.maxPriority() + PRIORITY_STEP;
            for (int i = 0; i < currentOrderedIds.size(); i++) {
                RoleId targetId = orderedIdList.get(i);
                RoleId currentId = currentOrderedIds.get(i);
                if (targetId.equals(currentId)) {
                    continue;
                }

                int targetIndex = indexById.get(targetId.value());
                int currentPriority = priorityById.get(currentId.value());
                int targetPriority = priorityById.get(targetId.value());

                updatePriorityOrThrow(targetId, temporaryPriority++, "暂态更新失败");
                updatePriorityOrThrow(currentId, targetPriority, "交换更新失败");
                updatePriorityOrThrow(targetId, currentPriority, "交换更新失败");

                priorityById.put(targetId.value(), currentPriority);
                priorityById.put(currentId.value(), targetPriority);

                currentOrderedIds.set(i, targetId);
                currentOrderedIds.set(targetIndex, currentId);
                indexById.put(targetId.value(), i);
                indexById.put(currentId.value(), targetIndex);
            }
        } catch (RuntimeException exception) {
            if (isConcurrentModification(exception)) {
                throw new ApiException(
                        ErrorCode.SORT_CONCURRENT_MODIFICATION.getCode(),
                        ErrorCode.SORT_CONCURRENT_MODIFICATION.getMessage());
            }
            throw new ApiException(ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessage());
        }
    }

    @Override
    @AuditLog(type = "Role", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "更新角色")
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(ChangeRoleInfoCommand command) {
        Role role = toRole(command);
        dao.update(role);
        afterWrite(role);
    }

    private void afterWrite(Role role) {
        dao.deleteRoleMenu(RoleIdCodec.toValue(role.getId()));
        if (role.getMenuIdList() != null && !role.getMenuIdList().isEmpty()) {
            dao.insertRoleMenu(RoleIdCodec.toValue(role.getId()), role.getMenuIdList());
        }

        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(AssignRoleUsersCommand command) {
        dao.deleteRoleUser(RoleIdCodec.toValue(command.getRoleId()));

        if (command.getUserIds() != null && !command.getUserIds().isEmpty()) {
            dao.insertRoleUser(
                    RoleIdCodec.toValue(command.getRoleId()),
                    command.getUserIds().stream().map(UserIdCodec::toValue).collect(Collectors.toList()));
        }

        notifyCacheChanged();
    }

    @Override
    @AuditLog(type = "Role", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "更新角色状态")
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(ChangeRoleStatusCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setStatus(command.getStatus());
        int result = dao.updateStatus(role);

        notifyCacheChanged();

        return result;
    }

    @AuditLog(
            type = "Role",
            id = "#command.id.value()",
            action = AuditAction.DELETE,
            summary = "删除角色",
            recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public int remove(DeleteRoleCommand command) {
        RoleId id = command.getId();
        Role role = get(id);
        if (role == null) {
            return 0;
        }

        dao.deleteRoleMenu(RoleIdCodec.toValue(id));
        dao.deleteRoleUser(RoleIdCodec.toValue(id));
        int retVal = dao.deleteById(id);

        notifyCacheChanged();

        return retVal;
    }

    @Override
    public List<User> listRoleUsers(RoleQuery query) {
        List<Long> userIdList = idUserIdsMapHandler
                .computeIfAbsent(HashMap::new)
                .computeIfAbsent(
                        RoleIdCodec.toValue(query.getId()),
                        roleId -> dao.listRoleUsers(RoleIdCodec.toValue(query.getId())));

        return userIdList.stream().map(this::newUser).collect(Collectors.toList());
    }

    @Override
    public List<Menu> listRoleMenus(RoleQuery query) {
        List<Long> menuIdList = idMenuIdsMapHandler
                .computeIfAbsent(HashMap::new)
                .computeIfAbsent(
                        RoleIdCodec.toValue(query.getId()),
                        roleId -> dao.listRoleMenus(RoleIdCodec.toValue(query.getId())));

        return menuIdList.stream().map(this::newMenu).collect(Collectors.toList());
    }

    private User newUser(Long id) {
        User user = new User();
        user.setId(UserIdCodec.toDomain(id));
        return user;
    }

    private Menu newMenu(Long id) {
        Menu menu = new Menu();
        menu.setId(MenuIdCodec.toDomain(id));
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

    private List<RoleId> normalizeOrderedIds(List<RoleId> orderedIds) throws ApiException {
        if (orderedIds == null) {
            return new ArrayList<>();
        }

        Set<Long> uniqueIdValues = new HashSet<>(orderedIds.size());
        List<RoleId> normalized = new ArrayList<>(orderedIds.size());
        for (RoleId orderedId : orderedIds) {
            if (orderedId == null || orderedId.value() == null) {
                throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
            }
            if (!uniqueIdValues.add(orderedId.value())) {
                throw new ApiException(ErrorCode.SORT_DUPLICATE_ID.getCode(), ErrorCode.SORT_DUPLICATE_ID.getMessage());
            }
            normalized.add(orderedId);
        }
        return normalized;
    }

    private List<Long> toValues(List<RoleId> ids) {
        List<Long> values = new ArrayList<>(ids.size());
        for (RoleId id : ids) {
            values.add(id.value());
        }
        return values;
    }

    private boolean isConcurrentModification(RuntimeException exception) {
        Throwable cursor = exception;
        while (cursor != null) {
            if (cursor instanceof SQLException) {
                return isConcurrentSqlFailure((SQLException) cursor);
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    private boolean isConcurrentSqlFailure(SQLException sqlException) {
        int errorCode = sqlException.getErrorCode();
        String sqlState = sqlException.getSQLState();
        if (errorCode == 1205 || errorCode == 1213 || errorCode == 1207) {
            return true;
        }
        if (errorCode == 1222) {
            return true;
        }
        return "55P03".equals(sqlState)
                || "40P01".equals(sqlState)
                || "40001".equals(sqlState)
                || "23505".equals(sqlState);
    }

    private void updatePriorityOrThrow(RoleId id, int priority, String message) throws ApiException {
        Role role = new Role();
        role.setId(id);
        role.setPriority(priority);
        int updated = dao.updatePriority(role);
        if (updated != 1) {
            throw new ApiException(ErrorCode.SORT_DB_FAILURE.getCode(), message);
        }
    }

    private Role toRole(CreateRoleCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setName(command.getName());
        role.setPrivilege(command.getPrivilege());
        role.setStatus(command.getStatus());
        role.setRemarks(command.getRemarks());
        role.setMenuIdList(MenuIdCodec.toValues(command.getMenuIdList()));
        return role;
    }

    private Role toRole(ChangeRoleInfoCommand command) {
        Role role = new Role();
        role.setId(command.getId());
        role.setName(command.getName());
        role.setPrivilege(command.getPrivilege());
        role.setStatus(command.getStatus());
        role.setRemarks(command.getRemarks());
        role.setMenuIdList(MenuIdCodec.toValues(command.getMenuIdList()));
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
