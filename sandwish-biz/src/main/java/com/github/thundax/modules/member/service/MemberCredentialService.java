package com.github.thundax.modules.member.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.MemberCredential;
import com.github.thundax.modules.member.entity.MemberIdentity;

public interface MemberCredentialService {

    MemberCredential getPasswordCredential(EntityId memberId);

    void upsertPassword(Member member, MemberIdentity identity, String encryptedPassword);
}
