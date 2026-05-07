package com.github.thundax.modules.member.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.entity.MemberRefreshToken;
import com.github.thundax.modules.member.entity.enums.MemberRefreshTokenStatus;
import java.util.List;

public interface MemberRefreshTokenDao {

    MemberRefreshToken getById(EntityId id);

    MemberRefreshToken getByTokenId(String tokenId);

    MemberRefreshToken getByTokenHash(String tokenHash);

    List<MemberRefreshToken> listByMemberIdAndStatus(EntityId memberId, MemberRefreshTokenStatus status);

    EntityId insert(MemberRefreshToken refreshToken);

    int update(MemberRefreshToken refreshToken);

    int updateStatus(MemberRefreshToken refreshToken);
}
