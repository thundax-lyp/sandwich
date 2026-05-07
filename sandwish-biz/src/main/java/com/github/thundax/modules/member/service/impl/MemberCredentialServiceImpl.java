package com.github.thundax.modules.member.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.dao.MemberCredentialDao;
import com.github.thundax.modules.member.dao.MemberIdentityDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.MemberCredential;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberCredentialStatus;
import com.github.thundax.modules.member.entity.enums.MemberCredentialType;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import com.github.thundax.modules.member.service.MemberCredentialService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberCredentialServiceImpl implements MemberCredentialService {

    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;

    private final MemberIdentityDao memberIdentityDao;
    private final MemberCredentialDao memberCredentialDao;

    public MemberCredentialServiceImpl(MemberIdentityDao memberIdentityDao, MemberCredentialDao memberCredentialDao) {
        this.memberIdentityDao = memberIdentityDao;
        this.memberCredentialDao = memberCredentialDao;
    }

    @Override
    public MemberCredential getPasswordCredential(EntityId memberId) {
        MemberIdentity identity = memberIdentityDao.getByMemberIdAndType(memberId, MemberIdentityType.ACCOUNT);
        return identity == null
                ? null
                : memberCredentialDao.getByIdentityIdAndType(identity.getId(), MemberCredentialType.PASSWORD);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertPassword(Member member, MemberIdentity identity, String encryptedPassword) {
        if (member == null || member.getId() == null || identity == null || StringUtils.isBlank(encryptedPassword)) {
            return;
        }
        MemberCredential credential =
                memberCredentialDao.getByIdentityIdAndType(identity.getId(), MemberCredentialType.PASSWORD);
        if (credential == null) {
            credential = new MemberCredential();
            credential.setMemberId(member.getId());
            credential.setIdentityId(identity.getId());
            credential.setCredentialType(MemberCredentialType.PASSWORD);
            credential.setCredentialValue(encryptedPassword);
            credential.setStatus(MemberCredentialStatus.ACTIVE);
            credential.setNeedChangePassword(false);
            credential.setFailedCount(0);
            credential.setFailedLimit(DEFAULT_PASSWORD_FAILED_LIMIT);
            credential.setId(memberCredentialDao.insert(credential));
            return;
        }

        credential.setCredentialValue(encryptedPassword);
        credential.setStatus(MemberCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        memberCredentialDao.update(credential);
    }
}
