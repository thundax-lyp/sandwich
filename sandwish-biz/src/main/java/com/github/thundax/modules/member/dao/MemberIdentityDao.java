package com.github.thundax.modules.member.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberIdentityStatus;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import java.util.List;

public interface MemberIdentityDao {

    MemberIdentity getById(EntityId id);

    MemberIdentity getByIdentity(MemberIdentityType identityType, String identityValue);

    MemberIdentity getByMemberIdAndType(EntityId memberId, MemberIdentityType identityType);

    List<MemberIdentity> listByMemberIdAndStatus(EntityId memberId, MemberIdentityStatus status);

    EntityId insert(MemberIdentity memberIdentity);

    int update(MemberIdentity memberIdentity);

    int updateStatus(MemberIdentity memberIdentity);
}
