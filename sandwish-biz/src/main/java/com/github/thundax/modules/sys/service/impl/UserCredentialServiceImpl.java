package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.UserCredentialDao;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.dao.UserIdentityDao;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserCredentialStatus;
import com.github.thundax.modules.sys.entity.enums.UserCredentialType;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import com.github.thundax.modules.sys.service.UserCredentialService;
import com.github.thundax.modules.sys.service.UserIdentityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserCredentialServiceImpl implements UserCredentialService {

    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;

    private final UserDao userDao;
    private final UserIdentityService userIdentityService;
    private final UserIdentityDao userIdentityDao;
    private final UserCredentialDao userCredentialDao;
    private final SignService signService;

    public UserCredentialServiceImpl(
            UserDao userDao,
            UserIdentityService userIdentityService,
            UserIdentityDao userIdentityDao,
            UserCredentialDao userCredentialDao,
            SignService signService) {
        this.userDao = userDao;
        this.userIdentityService = userIdentityService;
        this.userIdentityDao = userIdentityDao;
        this.userCredentialDao = userCredentialDao;
        this.signService = signService;
    }

    @Override
    public UserCredential getPasswordCredential(EntityId userId) {
        UserIdentity identity = userIdentityDao.getByUserIdAndType(userId, UserIdentityType.ACCOUNT);
        return identity == null
                ? null
                : userCredentialDao.getByIdentityIdAndType(identity.getId(), UserCredentialType.PASSWORD);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(EntityId userId, String encryptedPassword, String updateUserId) {
        User user = userId == null ? null : userDao.getById(userId);
        if (user == null) {
            return;
        }
        user.setUpdateUserId(updateUserId);
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
        UserIdentity accountIdentity =
                userIdentityService.updateAccountIdentity(user, userIdentityService.getAccountLoginName(userId));
        updatePasswordCredential(user, accountIdentity, encryptedPassword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePasswordCredential(User user, UserIdentity accountIdentity, String encryptedPassword) {
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
