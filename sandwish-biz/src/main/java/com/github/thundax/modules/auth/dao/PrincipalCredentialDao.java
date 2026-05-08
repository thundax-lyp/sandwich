package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalCredentialDao {

    PrincipalCredential getById(EntityId id);

    PrincipalCredential getByIdentityIdAndType(EntityId identityId, PrincipalCredentialType credentialType);

    PrincipalCredential getByPrincipalKeyAndType(PrincipalKey principalKey, PrincipalCredentialType credentialType);

    List<PrincipalCredential> listByPrincipalKeyAndStatus(PrincipalKey principalKey, PrincipalCredentialStatus status);

    EntityId insert(PrincipalCredential principalCredential);

    int update(PrincipalCredential principalCredential);

    int updateStatus(PrincipalCredential principalCredential);

    int updateVerifyState(PrincipalCredential principalCredential);
}
