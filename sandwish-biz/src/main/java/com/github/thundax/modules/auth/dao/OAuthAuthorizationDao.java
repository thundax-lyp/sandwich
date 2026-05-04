package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.OAuthAuthorization;

public interface OAuthAuthorizationDao {

    OAuthAuthorization getById(EntityId id);

    OAuthAuthorization getByAuthorizationCode(String authorizationCode);

    String insert(OAuthAuthorization authorization);

    int updateUsed(OAuthAuthorization authorization);

    int deleteByAuthorizationCode(String authorizationCode);
}
