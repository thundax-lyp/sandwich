package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.auth.dao.UserCredentialDao;
import com.github.thundax.modules.auth.dao.UserIdentityDao;
import com.github.thundax.modules.auth.entity.UserCredential;
import com.github.thundax.modules.auth.entity.UserIdentity;
import com.github.thundax.modules.auth.entity.enums.UserCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.UserCredentialType;
import com.github.thundax.modules.auth.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.UserIdentityType;
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
import java.util.Date;
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

    private final UserDao dao;
    private final SignService signService;
    private final UserEncryptService userEncryptService;
    private final UserIdentityDao userIdentityDao;
    private final UserCredentialDao userCredentialDao;

    public UserServiceImpl(
            UserDao dao,
            SignService signService,
            UserEncryptService userEncryptService,
            UserIdentityDao userIdentityDao,
            UserCredentialDao userCredentialDao) {
        this.dao = dao;
        this.signService = signService;
        this.userEncryptService = userEncryptService;
        this.userIdentityDao = userIdentityDao;
        this.userCredentialDao = userCredentialDao;
    }

    public User getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<User> list(User user) {
        return list((UserQuery) null);
    }

    public List<User> list(UserQuery query) {
        return dao.list(
                query == null ? null : query.getOfficeId(),
                query == null ? null : query.getLoginName(),
                query == null ? null : query.getName(),
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : superFlagValue(query.getPrivilege()));
    }

    public PageDTO<User> page(UserQuery query, PageDTO<User> page) {
        PageDTO<User> normalizedPage = normalizePage(page);
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
        UserIdentity accountIdentity = upsertAccountIdentity(user);
        if (added) {
            upsertPasswordCredential(user, accountIdentity);
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
        upsertPasswordCredential(user, upsertAccountIdentity(user));
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
        return UserPrivilege.SUPER == privilege ? Global.YES : null;
    }

    private UserIdentity upsertAccountIdentity(User user) {
        if (user == null || user.getId() == null || StringUtils.isBlank(user.getLoginName())) {
            return null;
        }
        UserIdentity identity = userIdentityDao.getByUserIdAndType(user.getId(), UserIdentityType.ACCOUNT);
        Date now = new Date();
        if (identity == null) {
            identity = new UserIdentity();
            identity.setUserId(user.getId());
            identity.setIdentityType(UserIdentityType.ACCOUNT);
            identity.setIdentityValue(user.getLoginName());
            identity.setStatus(UserIdentityStatus.ENABLED);
            identity.setCreateDate(now);
            identity.setUpdateDate(now);
            identity.setCreateUserId(user.getCreateUserId());
            identity.setUpdateUserId(user.getUpdateUserId());
            identity.setId(EntityIdCodec.toDomain(userIdentityDao.insert(identity)));
            return identity;
        }

        identity.setIdentityValue(user.getLoginName());
        identity.setStatus(UserIdentityStatus.ENABLED);
        identity.setUpdateDate(now);
        identity.setUpdateUserId(user.getUpdateUserId());
        userIdentityDao.update(identity);
        return identity;
    }

    private void upsertPasswordCredential(User user, UserIdentity accountIdentity) {
        if (user == null || accountIdentity == null || StringUtils.isBlank(user.getLoginPass())) {
            return;
        }
        UserCredential credential =
                userCredentialDao.getByIdentityIdAndType(accountIdentity.getId(), UserCredentialType.PASSWORD);
        Date now = new Date();
        if (credential == null) {
            credential = new UserCredential();
            credential.setUserId(user.getId());
            credential.setIdentityId(accountIdentity.getId());
            credential.setCredentialType(UserCredentialType.PASSWORD);
            credential.setCredentialValue(user.getLoginPass());
            credential.setStatus(UserCredentialStatus.ACTIVE);
            credential.setNeedChangePassword(false);
            credential.setFailedCount(0);
            credential.setFailedLimit(DEFAULT_PASSWORD_FAILED_LIMIT);
            credential.setCreateDate(now);
            credential.setUpdateDate(now);
            credential.setCreateUserId(user.getCreateUserId());
            credential.setUpdateUserId(user.getUpdateUserId());
            credential.setId(EntityIdCodec.toDomain(userCredentialDao.insert(credential)));
            return;
        }

        credential.setCredentialValue(user.getLoginPass());
        credential.setStatus(UserCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        credential.setUpdateDate(now);
        credential.setUpdateUserId(user.getUpdateUserId());
        userCredentialDao.update(credential);
    }
}
