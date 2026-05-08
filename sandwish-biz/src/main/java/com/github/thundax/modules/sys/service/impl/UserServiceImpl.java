package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
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

    private static final String LEGACY_SUPER_FLAG = "1";
    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;

    private final UserDao dao;
    private final PrincipalIdentityService principalIdentityService;
    private final PrincipalCredentialService principalCredentialService;

    public UserServiceImpl(
            UserDao dao,
            PrincipalIdentityService principalIdentityService,
            PrincipalCredentialService principalCredentialService) {
        this.dao = dao;
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
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
    public EntityId add(User user, String loginName, String encryptedPassword, List<Long> roleIdList) {
        user.setId(dao.insert(user));
        afterWrite(user, true, loginName, encryptedPassword, roleIdList);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User user, String loginName, List<Long> roleIdList) {
        dao.update(user);
        afterWrite(user, false, loginName, null, roleIdList);
    }

    private void afterWrite(
            User user, boolean added, String loginName, String encryptedPassword, List<Long> roleIdList) {
        if (roleIdList != null) {
            dao.deleteUserRole(EntityIdCodec.toValue(user.getId()));
            if (!roleIdList.isEmpty()) {
                dao.insertUserRole(EntityIdCodec.toValue(user.getId()), roleIdList);
            }
        }
        PrincipalIdentity accountIdentity = updateAccountIdentity(user, loginName);
        if (added) {
            upsertPassword(user, accountIdentity, encryptedPassword);
        }
    }

    private PrincipalIdentity updateAccountIdentity(User user, String loginName) {
        if (user == null || user.getId() == null || StringUtils.isBlank(loginName)) {
            return null;
        }
        PrincipalKey principalKey = PrincipalKey.of(PrincipalType.USER, user.getId());
        PrincipalIdentity identity =
                principalIdentityService.getByPrincipalKeyAndType(principalKey, PrincipalIdentityType.USER_ACCOUNT);
        if (identity == null) {
            identity = new PrincipalIdentity();
            identity.setPrincipalKey(principalKey);
            identity.setType(PrincipalIdentityType.USER_ACCOUNT);
            identity.setIdentityValue(loginName);
            identity.setStatus(PrincipalIdentityStatus.ENABLED);
            principalIdentityService.add(identity);
            return identity;
        }

        identity.setIdentityValue(loginName);
        identity.setStatus(PrincipalIdentityStatus.ENABLED);
        principalIdentityService.update(identity);
        return identity;
    }

    private void upsertPassword(User user, PrincipalIdentity accountIdentity, String encryptedPassword) {
        if (user == null || accountIdentity == null || StringUtils.isBlank(encryptedPassword)) {
            return;
        }
        PrincipalCredential credential = principalCredentialService.getByIdentityIdAndType(
                accountIdentity.getId(), PrincipalCredentialType.USER_PASSWORD);
        if (credential == null) {
            credential = new PrincipalCredential();
            credential.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, user.getId()));
            credential.setIdentityId(accountIdentity.getId());
            credential.setCredentialType(PrincipalCredentialType.USER_PASSWORD);
            credential.setCredentialValue(encryptedPassword);
            credential.setStatus(PrincipalCredentialStatus.ACTIVE);
            credential.setNeedChangePassword(false);
            credential.setFailedCount(0);
            credential.setFailedLimit(DEFAULT_PASSWORD_FAILED_LIMIT);
            principalCredentialService.add(credential);
            return;
        }

        credential.setCredentialValue(encryptedPassword);
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        principalCredentialService.update(credential);
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

    private String statusValue(UserStatus status) {
        return status == null ? null : status.value();
    }

    private String superFlagValue(UserPrivilege privilege) {
        return UserPrivilege.SUPER == privilege ? LEGACY_SUPER_FLAG : null;
    }
}
