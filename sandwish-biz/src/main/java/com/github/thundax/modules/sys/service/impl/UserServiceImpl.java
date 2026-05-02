package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.service.UserEncryptService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserDao dao;
    private final SignService signService;
    private final UserEncryptService userEncryptService;

    public UserServiceImpl(UserDao dao, SignService signService, UserEncryptService userEncryptService) {
        this.dao = dao;
        this.signService = signService;
        this.userEncryptService = userEncryptService;
    }

    @Override
    public Class<User> getElementType() {
        return User.class;
    }

    @Override
    public User newEntity(String id) {
        User user = new User();
        user.setId(EntityIdCodec.toDomain(id));
        return user;
    }

    @Override
    public User getById(User entity) {
        return entity == null ? null : getById(entity.getId());
    }

    @Override
    public User getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<User> batchGetByIds(List<String> ids) {
        return dao.batchGetByIds(ids);
    }

    @Override
    public List<User> list(User user) {
        return list((UserQuery) null);
    }

    @Override
    public List<User> list(UserQuery query) {
        return dao.list(
                query == null ? null : query.getOfficeId(),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : superFlagValue(query.getPrivilege()));
    }

    @Override
    public User getOne(User user) {
        List<User> users = list(user);
        return users == null || users.isEmpty() ? null : users.get(0);
    }

    @Override
    public Page<User> page(User user, Page<User> page) {
        return page((UserQuery) null, page);
    }

    @Override
    public Page<User> page(UserQuery query, Page<User> page) {
        Page<User> normalizedPage = normalizePage(page);
        IPage<User> dataPage = dao.page(
                query == null ? null : query.getOfficeId(),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : superFlagValue(query.getPrivilege()),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    public long count(User user) {
        List<User> users = list(user);
        return users == null ? 0 : users.size();
    }

    @Override
    public User getByLoginName(String loginName) {
        User user = dao.getByLoginName(loginName);
        if (user != null) {
            userEncryptService.getById(user.getId());
        }
        return user;
    }

    @Override
    public User getBySsoLoginName(String ssoLoginName) {
        User user = dao.getBySsoLoginName(ssoLoginName);
        if (user != null) {
            userEncryptService.getById(user.getId());
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(User user) {
        user.setId(EntityIdCodec.toDomain(dao.insert(user)));
        afterWrite(user, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User user) {
        dao.update(user);
        afterWrite(user, false);
    }

    private void afterWrite(User user, boolean added) {
        dao.deleteUserRole(EntityIdCodec.toValue(user.getId()));
        if (user.getRoleIdList() != null && !user.getRoleIdList().isEmpty()) {
            dao.insertUserRole(EntityIdCodec.toValue(user.getId()), user.getRoleIdList());
        }
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
        UserEncrypt userEncrypt = new UserEncrypt();
        userEncrypt.setId(user.getId());
        userEncrypt.setEmail(user.getEmail());
        userEncrypt.setMobile(user.getMobile());
        userEncrypt.setTel(user.getTel());
        if (added) {
            userEncryptService.add(userEncrypt);
        } else {
            userEncryptService.update(userEncrypt);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(User user) {
        dao.updateLoginPass(user);
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
        UserEncrypt userEncrypt = new UserEncrypt();
        userEncrypt.setId(user.getId());
        userEncrypt.setLoginPass(user.getLoginPass());
        userEncryptService.updateLoginPass(userEncrypt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(User user) {
        dao.updateLoginInfo(user);
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(User user) {
        int result = dao.updateStatus(user);

        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(List<User> list) {
        return batchOperate(list, this::updateStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(User user) {
        dao.deleteUserRole(EntityIdCodec.toValue(user.getId()));

        int result = dao.deleteById(user.getId());

        signService.deleteSign(user.getSignName(), user.getSignId());

        return result;
    }

    @Override
    public List<Role> listUserRoles(User user) {
        return dao.listUserRoles(EntityIdCodec.toValue(user.getId())).stream()
                .map(this::newRole)
                .collect(Collectors.toList());
    }

    private Role newRole(String id) {
        Role role = new Role();
        role.setId(EntityIdCodec.toDomain(id));
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<User> list) {
        return batchOperate(list, this::deleteById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(User user) {
        return dao.updatePriority(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<User> list) {
        return batchOperate(list, this::updatePriority);
    }

    private int batchOperate(Collection<User> collection, Function<User, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (User user : collection) {
                count += operator.apply(user);
            }
        }
        return count;
    }

    private Page<User> normalizePage(Page<User> page) {
        Page<User> normalizedPage = page == null ? new Page<>() : page;
        if (normalizedPage.getPageNo() < Page.FIRST_PAGE_INDEX) {
            normalizedPage.setPageNo(Page.FIRST_PAGE_INDEX);
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(Page.DEFAULT_PAGE_SIZE);
        }
        return normalizedPage;
    }

    private String statusValue(UserStatus status) {
        return status == null ? null : status.value();
    }

    private String superFlagValue(UserPrivilege privilege) {
        return UserPrivilege.SUPER == privilege ? Global.YES : null;
    }
}
