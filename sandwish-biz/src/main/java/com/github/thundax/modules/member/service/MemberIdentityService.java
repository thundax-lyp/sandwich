package com.github.thundax.modules.member.service;

import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;

public interface MemberIdentityService {

    MemberIdentity getByIdentity(MemberIdentityType identityType, String identityValue);

    MemberIdentity updateIdentity(Member member, MemberIdentityType identityType, String identityValue);
}
