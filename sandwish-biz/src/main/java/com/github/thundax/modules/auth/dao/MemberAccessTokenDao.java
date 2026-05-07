package com.github.thundax.modules.auth.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.MemberAccessToken;
import com.github.thundax.modules.auth.entity.enums.MemberAccessTokenStatus;
import java.util.List;

public interface MemberAccessTokenDao {

    MemberAccessToken getById(EntityId id);

    MemberAccessToken getByTokenId(String tokenId);

    MemberAccessToken getByTokenHash(String tokenHash);

    List<MemberAccessToken> listByMemberIdAndStatus(EntityId memberId, MemberAccessTokenStatus status);

    EntityId insert(MemberAccessToken accessToken);

    int update(MemberAccessToken accessToken);

    int updateStatus(MemberAccessToken accessToken);
}
