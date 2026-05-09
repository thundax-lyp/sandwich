package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import org.springframework.stereotype.Component;

@Component
public class RoleAuditObjectLoader implements AuditObjectLoader {

    private final RoleService roleService;

    public RoleAuditObjectLoader(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public String objectType() {
        return Role.BEAN_NAME;
    }

    @Override
    public Object load(String objectId) {
        RoleQuery query = new RoleQuery();
        query.setId(EntityIdCodec.toDomain(Long.valueOf(objectId)));
        return roleService.get(query);
    }
}
