package com.github.thundax.modules.member.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.MemberAuthSession;
import com.github.thundax.modules.member.entity.enums.MemberAuthSessionStatus;
import java.util.List;

public interface MemberAuthSessionDao {

    MemberAuthSession getById(EntityId id);

    MemberAuthSession getBySessionId(String sessionId);

    List<MemberAuthSession> listByMemberIdAndStatus(EntityId memberId, MemberAuthSessionStatus status);

    EntityId insert(MemberAuthSession authSession);

    int update(MemberAuthSession authSession);

    int updateStatus(MemberAuthSession authSession);

    int touch(MemberAuthSession authSession);
}
