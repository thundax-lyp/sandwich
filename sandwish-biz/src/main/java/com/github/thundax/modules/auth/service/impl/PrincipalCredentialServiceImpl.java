package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.dao.PrincipalCredentialDao;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PrincipalCredentialServiceImpl implements PrincipalCredentialService {

    private final PrincipalCredentialDao principalCredentialDao;

    public PrincipalCredentialServiceImpl(PrincipalCredentialDao principalCredentialDao) {
        this.principalCredentialDao = principalCredentialDao;
    }

    @Override
    public PrincipalCredential getById(EntityId id) {
        return principalCredentialDao.getById(id);
    }

    @Override
    public PrincipalCredential getByIdentityIdAndType(EntityId identityId, PrincipalCredentialType credentialType) {
        return principalCredentialDao.getByIdentityIdAndType(identityId, credentialType);
    }

    @Override
    public PrincipalCredential getByPrincipalKeyAndType(
            PrincipalKey principalKey, PrincipalCredentialType credentialType) {
        return principalCredentialDao.getByPrincipalKeyAndType(principalKey, credentialType);
    }

    @Override
    public List<PrincipalCredential> listByPrincipalKeyAndStatus(
            PrincipalKey principalKey, PrincipalCredentialStatus status) {
        return principalCredentialDao.listByPrincipalKeyAndStatus(principalKey, status);
    }

    @Override
    public EntityId add(PrincipalCredential principalCredential) {
        EntityId id = principalCredentialDao.insert(principalCredential);
        principalCredential.setId(id);
        return id;
    }

    @Override
    public void update(PrincipalCredential principalCredential) {
        principalCredentialDao.update(principalCredential);
    }

    @Override
    public void updateStatus(PrincipalCredential principalCredential) {
        principalCredentialDao.updateStatus(principalCredential);
    }

    @Override
    public void updateVerifyState(PrincipalCredential principalCredential) {
        principalCredentialDao.updateVerifyState(principalCredential);
    }
}
