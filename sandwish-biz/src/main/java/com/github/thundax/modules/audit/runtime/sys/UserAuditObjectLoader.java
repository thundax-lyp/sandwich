package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.UserQuery;
import org.springframework.stereotype.Component;

@Component
public class UserAuditObjectLoader implements AuditObjectLoader {

    private final UserService userService;

    public UserAuditObjectLoader(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String objectType() {
        return User.BEAN_NAME;
    }

    @Override
    public Object load(String objectId) {
        UserQuery query = new UserQuery();
        query.setId(EntityIdCodec.toDomain(Long.valueOf(objectId)));
        return userService.get(query);
    }
}
