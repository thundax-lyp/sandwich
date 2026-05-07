package com.github.thundax.modules.member.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.dao.MemberIdentityDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberIdentityStatus;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import com.github.thundax.modules.member.service.MemberIdentityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberIdentityServiceImpl implements MemberIdentityService {

    private final MemberIdentityDao memberIdentityDao;

    public MemberIdentityServiceImpl(MemberIdentityDao memberIdentityDao) {
        this.memberIdentityDao = memberIdentityDao;
    }

    @Override
    public MemberIdentity getByIdentity(MemberIdentityType identityType, String identityValue) {
        if (identityType == null || StringUtils.isBlank(identityValue)) {
            return null;
        }
        return memberIdentityDao.getByIdentity(identityType, identityValue);
    }

    @Override
    public MemberIdentity getByAccount(String account) {
        return getByIdentity(MemberIdentityType.ACCOUNT, account);
    }

    @Override
    public MemberIdentity getByMobile(String mobile) {
        return getByIdentity(MemberIdentityType.MOBILE, mobile);
    }

    @Override
    public MemberIdentity getByEmail(String email) {
        return getByIdentity(MemberIdentityType.EMAIL, email);
    }

    @Override
    public String getAccountIdentityValue(EntityId memberId) {
        MemberIdentity identity = memberIdentityDao.getByMemberIdAndType(memberId, MemberIdentityType.ACCOUNT);
        return identity == null ? null : identity.getIdentityValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberIdentity updateIdentity(Member member, MemberIdentityType identityType, String identityValue) {
        if (member == null || member.getId() == null || identityType == null || StringUtils.isBlank(identityValue)) {
            return null;
        }
        MemberIdentity identity = memberIdentityDao.getByMemberIdAndType(member.getId(), identityType);
        if (identity == null) {
            identity = new MemberIdentity();
            identity.setMemberId(member.getId());
            identity.setIdentityType(identityType);
            identity.setIdentityValue(identityValue);
            identity.setStatus(MemberIdentityStatus.ENABLED);
            identity.setId(memberIdentityDao.insert(identity));
            return identity;
        }

        identity.setIdentityValue(identityValue);
        identity.setStatus(MemberIdentityStatus.ENABLED);
        memberIdentityDao.update(identity);
        return identity;
    }
}
