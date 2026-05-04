package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.entity.enums.UserCredentialStatus;
import com.github.thundax.modules.sys.entity.enums.UserCredentialType;
import java.util.List;

public interface UserCredentialDao {

    UserCredential getById(EntityId id);

    UserCredential getByIdentityIdAndType(EntityId identityId, UserCredentialType credentialType);

    UserCredential getByUserIdAndType(EntityId userId, UserCredentialType credentialType);

    List<UserCredential> listByUserIdAndStatus(EntityId userId, UserCredentialStatus status);

    String insert(UserCredential userCredential);

    int update(UserCredential userCredential);

    int updateStatus(UserCredential userCredential);

    int updateVerifyState(UserCredential userCredential);
}
