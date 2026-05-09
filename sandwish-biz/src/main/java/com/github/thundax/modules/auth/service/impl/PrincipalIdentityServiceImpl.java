package com.github.thundax.modules.auth.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.dao.PrincipalIdentityDao;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.command.PrincipalIdentityCommand;
import com.github.thundax.modules.auth.service.query.PrincipalIdentityQuery;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PrincipalIdentityServiceImpl implements PrincipalIdentityService {

    private final PrincipalIdentityDao principalIdentityDao;

    public PrincipalIdentityServiceImpl(PrincipalIdentityDao principalIdentityDao) {
        this.principalIdentityDao = principalIdentityDao;
    }

    @Override
    public PrincipalIdentity get(PrincipalIdentityQuery query) {
        if (query == null) {
            return null;
        }
        if (query.getId() != null) {
            return principalIdentityDao.getById(query.getId());
        }
        if (query.getIdentityType() != null && StringUtils.isNotBlank(query.getIdentityValue())) {
            return principalIdentityDao.getByIdentity(query.getIdentityType(), query.getIdentityValue());
        }
        if (query.getPrincipalKey() != null && query.getIdentityType() != null) {
            return principalIdentityDao.getByPrincipalKeyAndType(query.getPrincipalKey(), query.getIdentityType());
        }
        return null;
    }

    @Override
    public List<PrincipalIdentity> list(PrincipalIdentityQuery query) {
        return principalIdentityDao.listByPrincipalKeyAndStatus(query.getPrincipalKey(), query.getStatus());
    }

    @Override
    public EntityId create(PrincipalIdentityCommand command) {
        PrincipalIdentity principalIdentity = command.getPrincipalIdentity();
        EntityId id = principalIdentityDao.insert(principalIdentity);
        principalIdentity.setId(id);
        return id;
    }

    @Override
    public void change(PrincipalIdentityCommand command) {
        principalIdentityDao.update(command.getPrincipalIdentity());
    }

    @Override
    public void changeStatus(PrincipalIdentityCommand command) {
        principalIdentityDao.updateStatus(command.getPrincipalIdentity());
    }
}
