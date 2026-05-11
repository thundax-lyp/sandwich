package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.BizException;
import com.github.thundax.common.exception.ErrorCode;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.exception.BizExceptionBoundary;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.RoleId;
import com.github.thundax.modules.sys.entity.valueobject.RoleIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.command.ChangeUserInfoCommand;
import com.github.thundax.modules.sys.service.command.ChangeUserStatusCommand;
import com.github.thundax.modules.sys.service.command.CreateUserCommand;
import com.github.thundax.modules.sys.service.command.DeleteUserCommand;
import com.github.thundax.modules.sys.service.command.UserSortCommand;
import com.github.thundax.modules.sys.service.handler.UserDeleteCascadeHandler;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class UserServiceImpl implements UserService {

    private static final int PRIORITY_STEP = 10;

    private final UserDao dao;
    private final List<UserDeleteCascadeHandler> deleteCascadeHandlers;

    public UserServiceImpl(UserDao dao, List<UserDeleteCascadeHandler> deleteCascadeHandlers) {
        this.dao = dao;
        this.deleteCascadeHandlers = deleteCascadeHandlers == null ? Collections.emptyList() : deleteCascadeHandlers;
    }

    public User get(UserId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<User> list(UserQuery query) {
        return dao.list(
                query == null ? null : DepartmentIdCodec.toValue(query.getDepartmentId()),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege());
    }

    public PageResult<User> page(UserQuery query, PageQuery page) {
        IPage<User> dataPage = dao.page(
                query == null ? null : DepartmentIdCodec.toValue(query.getDepartmentId()),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(UserSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<UserId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_EMPTY_INPUT.getCode(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        List<User> selectedUsers = dao.listByIds(toValues(orderedIdList));
        if (selectedUsers == null || selectedUsers.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        List<User> currentUsers = dao.list(null, null, null, null, null, effectiveDirection);
        if (currentUsers == null || currentUsers.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        if (currentUsers.size() != orderedIdList.size()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        Map<Long, Integer> indexById = new HashMap<>(currentUsers.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentUsers.size());
        List<UserId> currentOrderedIds = new ArrayList<>(currentUsers.size());
        for (int i = 0; i < currentUsers.size(); i++) {
            User currentUser = currentUsers.get(i);
            if (currentUser == null || currentUser.getId() == null) {
                throw new BizException(
                        ErrorCode.SORT_DB_FAILURE.getCode(),
                        ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                        ErrorCode.SORT_DB_FAILURE.getMessage());
            }
            long userId = currentUser.getId().value();
            indexById.put(userId, i);
            priorityById.put(userId, currentUser.getPriority());
            currentOrderedIds.add(currentUser.getId());
        }

        for (UserId orderedId : orderedIdList) {
            if (!indexById.containsKey(orderedId.value())) {
                throw new BizException(
                        ErrorCode.SORT_MISSING_ID.getCode(),
                        ErrorCode.SORT_MISSING_ID.getMessageKey(),
                        ErrorCode.SORT_MISSING_ID.getMessage());
            }
        }

        int temporaryPriority = dao.maxPriority() + PRIORITY_STEP;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            UserId targetId = orderedIdList.get(i);
            UserId currentId = currentOrderedIds.get(i);
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
    }

    @Override
    @AuditLog(type = "User", id = "", action = AuditAction.CREATE, summary = "创建后台用户", recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public UserId create(CreateUserCommand command) {
        User user = toUser(command);
        user.setId(dao.insert(user));
        rewriteUserRoles(user.getId(), command.getRoleIdList());
        return user.getId();
    }

    @Override
    @AuditLog(type = "User", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "更新后台用户")
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(ChangeUserInfoCommand command) {
        User user = toUser(command);
        dao.update(user);
        rewriteUserRoles(user.getId(), command.getRoleIdList());
    }

    private void rewriteUserRoles(UserId userId, List<RoleId> roleIdList) {
        if (roleIdList != null) {
            dao.deleteUserRole(UserIdCodec.toValue(userId));
            if (!roleIdList.isEmpty()) {
                dao.insertUserRole(UserIdCodec.toValue(userId), RoleIdCodec.toValues(roleIdList));
            }
        }
    }

    @Override
    @AuditLog(type = "User", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "更新后台用户状态")
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(ChangeUserStatusCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setStatus(command.getStatus());
        return dao.updateStatus(user);
    }

    @AuditLog(
            type = "User",
            id = "#command.id.value()",
            action = AuditAction.DELETE,
            summary = "删除后台用户",
            recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public int remove(DeleteUserCommand command) {
        User user = get(command.getId());
        if (user == null) {
            return 0;
        }

        for (UserDeleteCascadeHandler deleteCascadeHandler : deleteCascadeHandlers) {
            deleteCascadeHandler.beforeDelete(user);
        }
        dao.deleteUserRole(UserIdCodec.toValue(command.getId()));

        return dao.deleteById(command.getId());
    }

    @Override
    public List<Role> listUserRoles(UserQuery query) {
        return dao.listUserRoles(UserIdCodec.toValue(query.getId())).stream()
                .map(this::newRole)
                .collect(Collectors.toList());
    }

    private Role newRole(Long id) {
        Role role = new Role();
        role.setId(RoleIdCodec.toDomain(id));
        return role;
    }

    private User toUser(CreateUserCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setDepartmentId(command.getDepartmentId());
        user.setEmail(command.getEmail());
        user.setMobile(command.getMobile());
        user.setTel(command.getTel());
        user.setName(command.getName());
        user.setRank(command.getRank());
        user.setPrivilege(command.getPrivilege());
        user.setStatus(command.getStatus());
        user.setRemarks(command.getRemarks());
        return user;
    }

    private User toUser(ChangeUserInfoCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setDepartmentId(command.getDepartmentId());
        user.setEmail(command.getEmail());
        user.setMobile(command.getMobile());
        user.setTel(command.getTel());
        user.setName(command.getName());
        user.setRank(command.getRank());
        user.setPrivilege(command.getPrivilege());
        user.setStatus(command.getStatus());
        user.setRemarks(command.getRemarks());
        return user;
    }

    private List<Long> toValues(List<UserId> ids) {
        List<Long> values = new ArrayList<>(ids.size());
        for (UserId id : ids) {
            values.add(id.value());
        }
        return values;
    }

    private void updatePriorityOrThrow(UserId id, int priority, String message) {
        User user = new User();
        user.setId(id);
        user.setPriority(priority);

        int updated = dao.updatePriority(user);
        if (updated != 1) {
            throw new BizException(
                    ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessageKey(), message);
        }
    }
}
