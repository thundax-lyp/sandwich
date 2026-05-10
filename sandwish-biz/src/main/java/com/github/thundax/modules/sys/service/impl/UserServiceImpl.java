package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
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
import com.github.thundax.modules.sys.service.handler.UserDeleteCascadeHandler;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

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
        PageQuery normalizedPage = normalizePage(page);
        IPage<User> dataPage = dao.page(
                query == null ? null : DepartmentIdCodec.toValue(query.getDepartmentId()),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
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
}
