package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class UserAuditObjectLoader implements AuditObjectLoader {

    private static final String OBJECT_TYPE = "User";

    private final UserService userService;

    public UserAuditObjectLoader(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String objectType() {
        return OBJECT_TYPE;
    }

    @Override
    public Object load(String objectId) {
        return userService.get(UserIdCodec.toDomain(Long.valueOf(objectId)));
    }
}
