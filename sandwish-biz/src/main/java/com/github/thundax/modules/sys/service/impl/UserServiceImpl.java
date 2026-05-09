package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
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

    public User get(UserQuery query) {
        if (query == null || query.getId() == null) {
            return null;
        }
        return dao.getById(query.getId());
    }

    public List<User> list(UserQuery query) {
        return dao.list(
                query == null ? null : EntityIdCodec.toValue(query.getDepartmentId()),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege());
    }

    public PageResult<User> page(UserQuery query, PageQuery page) {
        PageQuery normalizedPage = normalizePage(page);
        IPage<User> dataPage = dao.page(
                query == null ? null : EntityIdCodec.toValue(query.getDepartmentId()),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(),
                (int) dataPage.getSize(),
                dataPage.getTotal(),
                dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId create(CreateUserCommand command) {
        User user = toUser(command);
        user.setId(dao.insert(user));
        rewriteUserRoles(user.getId(), command.getRoleIdList());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(ChangeUserInfoCommand command) {
        User user = toUser(command);
        dao.update(user);
        rewriteUserRoles(user.getId(), command.getRoleIdList());
    }

    private void rewriteUserRoles(EntityId userId, List<EntityId> roleIdList) {
        if (roleIdList != null) {
            dao.deleteUserRole(EntityIdCodec.toValue(userId));
            if (!roleIdList.isEmpty()) {
                dao.insertUserRole(EntityIdCodec.toValue(userId), EntityIdCodec.toValues(roleIdList));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(ChangeUserStatusCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setStatus(command.getStatus());
        return dao.updateStatus(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public int remove(DeleteUserCommand command) {
        UserQuery query = new UserQuery();
        query.setId(command.getId());
        User user = get(query);
        if (user == null) {
            return 0;
        }

        for (UserDeleteCascadeHandler deleteCascadeHandler : deleteCascadeHandlers) {
            deleteCascadeHandler.beforeDelete(user);
        }
        dao.deleteUserRole(EntityIdCodec.toValue(command.getId()));

        return dao.deleteById(command.getId());
    }

    @Override
    public List<Role> listUserRoles(UserQuery query) {
        return dao.listUserRoles(EntityIdCodec.toValue(query.getId())).stream()
                .map(this::newRole)
                .collect(Collectors.toList());
    }

    private Role newRole(Long id) {
        Role role = new Role();
        role.setId(EntityIdCodec.toDomain(id));
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
        user.setDepartmentId(EntityIdCodec.toValue(command.getDepartmentId()));
        user.setEmail(command.getEmail());
        user.setMobile(command.getMobile());
        user.setTel(command.getTel());
        user.setName(command.getName());
        user.setRank(command.getRank());
        user.setPrivilege(command.getPrivilege());
        user.setStatus(command.getStatus());
        user.setPriority(command.getPriority());
        user.setRemarks(command.getRemarks());
        return user;
    }

    private User toUser(ChangeUserInfoCommand command) {
        User user = new User();
        user.setId(command.getId());
        user.setDepartmentId(EntityIdCodec.toValue(command.getDepartmentId()));
        user.setEmail(command.getEmail());
        user.setMobile(command.getMobile());
        user.setTel(command.getTel());
        user.setName(command.getName());
        user.setRank(command.getRank());
        user.setPrivilege(command.getPrivilege());
        user.setStatus(command.getStatus());
        user.setPriority(command.getPriority());
        user.setRemarks(command.getRemarks());
        return user;
    }
}
