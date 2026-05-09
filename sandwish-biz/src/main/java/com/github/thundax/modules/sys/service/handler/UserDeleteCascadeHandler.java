package com.github.thundax.modules.sys.service.handler;

import com.github.thundax.modules.sys.entity.User;

public interface UserDeleteCascadeHandler {

    void beforeDelete(User user);
}
