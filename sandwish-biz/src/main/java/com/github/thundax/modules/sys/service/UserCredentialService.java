package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.UserCredential;

public interface UserCredentialService {

    UserCredential getPasswordCredential(EntityId userId);

    void updatePassword(EntityId userId, String encryptedPassword, String updateUserId);
}
