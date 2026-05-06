package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.dao.UserIdentityDao;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import com.github.thundax.modules.sys.service.UserIdentityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserIdentityServiceImpl implements UserIdentityService {

    private final UserIdentityDao userIdentityDao;

    public UserIdentityServiceImpl(UserIdentityDao userIdentityDao) {
        this.userIdentityDao = userIdentityDao;
    }

    @Override
    public UserIdentity getByLoginName(String loginName) {
        return userIdentityDao.getByIdentity(UserIdentityType.ACCOUNT, loginName);
    }

    @Override
    public String getAccountLoginName(EntityId userId) {
        UserIdentity identity = userIdentityDao.getByUserIdAndType(userId, UserIdentityType.ACCOUNT);
        return identity == null ? null : identity.getIdentityValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserIdentity updateAccountIdentity(User user, String loginName) {
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
            identity.setId(EntityId.of(userIdentityDao.insert(identity)));
            return identity;
        }

        identity.setIdentityValue(loginName);
        identity.setStatus(UserIdentityStatus.ENABLED);
        userIdentityDao.update(identity);
        return identity;
    }
}
