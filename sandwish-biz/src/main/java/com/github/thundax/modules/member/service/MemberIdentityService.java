package com.github.thundax.modules.member.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;

public interface MemberIdentityService {

    MemberIdentity getByIdentity(MemberIdentityType identityType, String identityValue);

    MemberIdentity getByAccount(String account);

    MemberIdentity getByMobile(String mobile);

    MemberIdentity getByEmail(String email);

    String getAccountIdentityValue(EntityId memberId);

    MemberIdentity updateIdentity(Member member, MemberIdentityType identityType, String identityValue);
}
