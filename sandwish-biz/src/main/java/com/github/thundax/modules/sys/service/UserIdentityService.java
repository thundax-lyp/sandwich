package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserIdentity;

public interface UserIdentityService {

    UserIdentity getByLoginName(String loginName);

    String getAccountLoginName(EntityId userId);

    UserIdentity updateAccountIdentity(User user, String loginName);
}
