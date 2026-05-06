package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.UserCredentialDao;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.dao.UserIdentityDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserCredentialStatus;
import com.github.thundax.modules.sys.entity.enums.UserCredentialType;
import com.github.thundax.modules.sys.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;
    private static final String LEGACY_SUPER_FLAG = "1";

    private final UserDao dao;
    private final SignService signService;
    private final UserIdentityDao userIdentityDao;
    private final UserCredentialDao userCredentialDao;

    public UserServiceImpl(
            UserDao dao,
            SignService signService,
            UserIdentityDao userIdentityDao,
            UserCredentialDao userCredentialDao) {
        this.dao = dao;
        this.signService = signService;
        this.userIdentityDao = userIdentityDao;
        this.userCredentialDao = userCredentialDao;
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
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : superFlagValue(query.getPrivilege()));
    }

    public PageDTO<User> page(UserQuery query, PageDTO<User> page) {
        PageDTO<User> normalizedPage = normalizePage(page);
        IPage<User> dataPage = dao.page(
                query == null ? null : query.getDepartmentId(),
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
    @Transactional(rollbackFor = Exception.class)
    public EntityId add(User user, String loginName, String encryptedPassword, List<String> roleIdList) {
        user.setId(EntityIdCodec.toDomain(dao.insert(user)));
        afterWrite(user, true, loginName, encryptedPassword, roleIdList);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User user, String loginName, List<String> roleIdList) {
        dao.update(user);
        afterWrite(user, false, loginName, null, roleIdList);
    }

    private void afterWrite(
            User user, boolean added, String loginName, String encryptedPassword, List<String> roleIdList) {
        if (roleIdList != null) {
            dao.deleteUserRole(EntityIdCodec.toValue(user.getId()));
            if (!roleIdList.isEmpty()) {
                dao.insertUserRole(EntityIdCodec.toValue(user.getId()), roleIdList);
            }
        }
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
        UserIdentity accountIdentity = upsertAccountIdentity(user, loginName);
        if (added) {
            upsertPasswordCredential(user, accountIdentity, encryptedPassword);
        }
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
    public int batchUpdateStatus(List<User> list) {
        return batchOperate(list, this::updateStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        User user = getById(id);
        if (user == null) {
            return 0;
        }

        dao.deleteUserRole(EntityIdCodec.toValue(id));

        int result = dao.deleteById(id);

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

    private String statusValue(UserStatus status) {
        return status == null ? null : status.value();
    }

    private String superFlagValue(UserPrivilege privilege) {
        return UserPrivilege.SUPER == privilege ? LEGACY_SUPER_FLAG : null;
    }

    private UserIdentity upsertAccountIdentity(User user, String loginName) {
        if (user == null || user.getId() == null || StringUtils.isBlank(loginName)) {
            return null;
        }
        UserIdentity identity = userIdentityDao.getByUserIdAndType(user.getId(), UserIdentityType.ACCOUNT);
        if (identity == null) {
            identity = new UserIdentity();
            identity.setUserId(user.getId());
            identity.setIdentityType(UserIdentityType.ACCOUNT);
            identity.setIdentityValue(loginName);
            identity.setStatus(UserIdentityStatus.ENABLED);
            identity.setId(EntityIdCodec.toDomain(userIdentityDao.insert(identity)));
            return identity;
        }

        identity.setIdentityValue(loginName);
        identity.setStatus(UserIdentityStatus.ENABLED);
        userIdentityDao.update(identity);
        return identity;
    }

    private void upsertPasswordCredential(User user, UserIdentity accountIdentity, String encryptedPassword) {
        if (user == null || accountIdentity == null || StringUtils.isBlank(encryptedPassword)) {
            return;
        }
        UserCredential credential =
                userCredentialDao.getByIdentityIdAndType(accountIdentity.getId(), UserCredentialType.PASSWORD);
        if (credential == null) {
            credential = new UserCredential();
            credential.setUserId(user.getId());
            credential.setIdentityId(accountIdentity.getId());
            credential.setCredentialType(UserCredentialType.PASSWORD);
            credential.setCredentialValue(encryptedPassword);
            credential.setStatus(UserCredentialStatus.ACTIVE);
            credential.setNeedChangePassword(false);
            credential.setFailedCount(0);
            credential.setFailedLimit(DEFAULT_PASSWORD_FAILED_LIMIT);
            credential.setId(EntityIdCodec.toDomain(userCredentialDao.insert(credential)));
            return;
        }

        credential.setCredentialValue(encryptedPassword);
        credential.setStatus(UserCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        userCredentialDao.update(credential);
    }
}
