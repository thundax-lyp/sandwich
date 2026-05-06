package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;

public interface UserCredentialService {

    UserCredential getPasswordCredential(EntityId userId);

    void upsertPassword(User user, String encryptedPassword);
}
