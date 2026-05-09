package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.handler.UserDeleteCascadeHandler;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
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

    public User getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<User> listAll() {
        return list((UserQuery) null);
    }

    public List<User> list(UserQuery query) {
        return dao.list(
                query == null ? null : query.getDepartmentId(),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege());
    }

    public PageDTO<User> page(UserQuery query, PageDTO<User> page) {
        PageDTO<User> normalizedPage = normalizePage(page);
        IPage<User> dataPage = dao.page(
                query == null ? null : query.getDepartmentId(),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : query.getStatus(),
                query == null ? null : query.getPrivilege(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId add(User user, String loginName, String encryptedPassword, List<Long> roleIdList) {
        user.setId(dao.insert(user));
        rewriteUserRoles(user, roleIdList);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User user, String loginName, List<Long> roleIdList) {
        dao.update(user);
        rewriteUserRoles(user, roleIdList);
    }

    private void rewriteUserRoles(User user, List<Long> roleIdList) {
        if (roleIdList != null) {
            dao.deleteUserRole(EntityIdCodec.toValue(user.getId()));
            if (!roleIdList.isEmpty()) {
                dao.insertUserRole(EntityIdCodec.toValue(user.getId()), roleIdList);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(User user) {
        return dao.updateStatus(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<User> list) {
        return batchOperate(list, this::updateStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        User user = getById(id);
        if (user == null) {
            return 0;
        }

        for (UserDeleteCascadeHandler deleteCascadeHandler : deleteCascadeHandlers) {
            deleteCascadeHandler.beforeDelete(user);
        }
        dao.deleteUserRole(EntityIdCodec.toValue(id));

        return dao.deleteById(id);
    }

    @Override
    public List<Role> listUserRoles(User user) {
        return dao.listUserRoles(EntityIdCodec.toValue(user.getId())).stream()
                .map(this::newRole)
                .collect(Collectors.toList());
    }

    private Role newRole(Long id) {
        Role role = new Role();
        role.setId(EntityIdCodec.toDomain(id));
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
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

    private PageDTO<User> normalizePage(PageDTO<User> page) {
        PageDTO<User> normalizedPage = page == null ? new PageDTO<>() : page;
        if (normalizedPage.getPageNo() < PageRules.firstPageIndex()) {
            normalizedPage.setPageNo(PageRules.firstPageIndex());
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(PageRules.defaultPageSize());
        }
        return normalizedPage;
    }
}
