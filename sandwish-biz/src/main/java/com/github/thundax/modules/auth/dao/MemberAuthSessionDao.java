package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.MemberAuthSession;
import com.github.thundax.modules.auth.entity.enums.MemberAuthSessionStatus;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import java.util.List;

public interface MemberAuthSessionDao {

    MemberAuthSession getById(EntityId id);

    List<MemberAuthSession> listByPrincipalKeyAndStatus(PrincipalKey principalKey, MemberAuthSessionStatus status);

    EntityId insert(MemberAuthSession authSession);

    int update(MemberAuthSession authSession);

    int updateStatus(MemberAuthSession authSession);

    int touch(MemberAuthSession authSession);
}
