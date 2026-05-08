package com.github.thundax.modules.auth.dao;

import com.github.thundax.modules.auth.entity.PrincipalLoginEvent;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalLoginEventId;

public interface PrincipalLoginEventDao {

    PrincipalLoginEvent getById(PrincipalLoginEventId id);

    PrincipalLoginEventId insert(PrincipalLoginEvent event);
}
