package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.OAuthClient;
import com.github.thundax.modules.auth.entity.enums.OAuthClientStatus;

public interface OAuthClientDao {

    OAuthClient getById(EntityId id);

    OAuthClient getByClientId(String clientId);

    OAuthClient getByClientIdAndStatus(String clientId, OAuthClientStatus status);

    EntityId insert(OAuthClient client);

    int update(OAuthClient client);
}
