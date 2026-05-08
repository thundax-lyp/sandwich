package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.dao.PrincipalIdentityDao;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PrincipalIdentityServiceImpl implements PrincipalIdentityService {

    private final PrincipalIdentityDao principalIdentityDao;

    public PrincipalIdentityServiceImpl(PrincipalIdentityDao principalIdentityDao) {
        this.principalIdentityDao = principalIdentityDao;
    }

    @Override
    public PrincipalIdentity getById(EntityId id) {
        return principalIdentityDao.getById(id);
    }

    @Override
    public PrincipalIdentity getByIdentity(PrincipalIdentityType identityType, String identityValue) {
        return principalIdentityDao.getByIdentity(identityType, identityValue);
    }

    @Override
    public PrincipalIdentity getByPrincipalKeyAndType(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        return principalIdentityDao.getByPrincipalKeyAndType(principalKey, identityType);
    }

    @Override
    public List<PrincipalIdentity> listByPrincipalKeyAndStatus(
            PrincipalKey principalKey, PrincipalIdentityStatus status) {
        return principalIdentityDao.listByPrincipalKeyAndStatus(principalKey, status);
    }

    @Override
    public EntityId add(PrincipalIdentity principalIdentity) {
        EntityId id = principalIdentityDao.insert(principalIdentity);
        principalIdentity.setId(id);
        return id;
    }

    @Override
    public void update(PrincipalIdentity principalIdentity) {
        principalIdentityDao.update(principalIdentity);
    }

    @Override
    public void updateStatus(PrincipalIdentity principalIdentity) {
        principalIdentityDao.updateStatus(principalIdentity);
    }
}
