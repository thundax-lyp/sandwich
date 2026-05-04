package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.UserIdentity;
import com.github.thundax.modules.sys.entity.enums.UserIdentityStatus;
import com.github.thundax.modules.sys.entity.enums.UserIdentityType;
import java.util.List;

public interface UserIdentityDao {

    UserIdentity getById(EntityId id);

    UserIdentity getByIdentity(UserIdentityType identityType, String identityValue);

    UserIdentity getByUserIdAndType(EntityId userId, UserIdentityType identityType);

    List<UserIdentity> listByUserIdAndStatus(EntityId userId, UserIdentityStatus status);

    String insert(UserIdentity userIdentity);

    int update(UserIdentity userIdentity);

    int updateStatus(UserIdentity userIdentity);
}
