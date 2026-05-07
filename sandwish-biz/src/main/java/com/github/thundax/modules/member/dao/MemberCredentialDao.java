package com.github.thundax.modules.member.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.MemberCredential;
import com.github.thundax.modules.member.entity.enums.MemberCredentialStatus;
import com.github.thundax.modules.member.entity.enums.MemberCredentialType;
import java.util.List;

public interface MemberCredentialDao {

    MemberCredential getById(EntityId id);

    MemberCredential getByIdentityIdAndType(EntityId identityId, MemberCredentialType credentialType);

    MemberCredential getByMemberIdAndType(EntityId memberId, MemberCredentialType credentialType);

    List<MemberCredential> listByMemberIdAndStatus(EntityId memberId, MemberCredentialStatus status);

    EntityId insert(MemberCredential memberCredential);

    int update(MemberCredential memberCredential);

    int updateStatus(MemberCredential memberCredential);

    int updateVerifyState(MemberCredential memberCredential);
}
