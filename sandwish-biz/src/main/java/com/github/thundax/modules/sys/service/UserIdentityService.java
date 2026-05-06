package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.User;

public interface UserIdentityService {

    User getByLoginName(String loginName);

    String getAccountLoginName(EntityId userId);
}
