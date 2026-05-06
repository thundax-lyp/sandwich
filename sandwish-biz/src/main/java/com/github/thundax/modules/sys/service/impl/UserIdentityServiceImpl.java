package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.dao.UserIdentityDao;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import com.github.thundax.modules.sys.service.UserIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserIdentityServiceImpl implements UserIdentityService {

    private final UserDao userDao;
    private final UserIdentityDao userIdentityDao;

    public UserIdentityServiceImpl(UserDao userDao, UserIdentityDao userIdentityDao) {
        this.userDao = userDao;
        this.userIdentityDao = userIdentityDao;
    }

    @Override
    public User getByLoginName(String loginName) {
        UserIdentity identity = userIdentityDao.getByIdentity(UserIdentityType.ACCOUNT, loginName);
        return identity == null ? null : userDao.getById(identity.getUserId());
    }

    @Override
    public String getAccountLoginName(EntityId userId) {
        UserIdentity identity = userIdentityDao.getByUserIdAndType(userId, UserIdentityType.ACCOUNT);
        return identity == null ? null : identity.getIdentityValue();
    }
}
