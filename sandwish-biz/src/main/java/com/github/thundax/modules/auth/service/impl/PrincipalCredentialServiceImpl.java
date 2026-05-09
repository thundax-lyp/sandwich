package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.dao.PrincipalCredentialDao;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.command.PrincipalCredentialCommand;
import com.github.thundax.modules.auth.service.query.PrincipalCredentialQuery;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PrincipalCredentialServiceImpl implements PrincipalCredentialService {

    private final PrincipalCredentialDao principalCredentialDao;

    public PrincipalCredentialServiceImpl(PrincipalCredentialDao principalCredentialDao) {
        this.principalCredentialDao = principalCredentialDao;
    }

    @Override
    public PrincipalCredential get(PrincipalCredentialQuery query) {
        if (query == null) {
            return null;
        }
        if (query.getId() != null) {
            return principalCredentialDao.getById(query.getId());
        }
        if (query.getIdentityId() != null && query.getCredentialType() != null) {
            return principalCredentialDao.getByIdentityIdAndType(query.getIdentityId(), query.getCredentialType());
        }
        if (query.getPrincipalKey() != null && query.getCredentialType() != null) {
            return principalCredentialDao.getByPrincipalKeyAndType(query.getPrincipalKey(), query.getCredentialType());
        }
        return null;
    }

    @Override
    public List<PrincipalCredential> list(PrincipalCredentialQuery query) {
        return principalCredentialDao.listByPrincipalKeyAndStatus(query.getPrincipalKey(), query.getStatus());
    }

    @Override
    public EntityId create(PrincipalCredentialCommand command) {
        PrincipalCredential principalCredential = command.getPrincipalCredential();
        EntityId id = principalCredentialDao.insert(principalCredential);
        principalCredential.setId(id);
        return id;
    }

    @Override
    public void change(PrincipalCredentialCommand command) {
        principalCredentialDao.update(command.getPrincipalCredential());
    }

    @Override
    public void changeStatus(PrincipalCredentialCommand command) {
        principalCredentialDao.updateStatus(command.getPrincipalCredential());
    }

    @Override
    public void changeVerifyState(PrincipalCredentialCommand command) {
        principalCredentialDao.updateVerifyState(command.getPrincipalCredential());
    }
}
