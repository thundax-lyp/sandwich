package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.entity.UserIdentity;

public interface UserCredentialService {

    UserCredential getPasswordCredential(EntityId userId);

    void updatePassword(EntityId userId, String encryptedPassword, String updateUserId);

    void updatePasswordCredential(User user, UserIdentity accountIdentity, String encryptedPassword);
}
