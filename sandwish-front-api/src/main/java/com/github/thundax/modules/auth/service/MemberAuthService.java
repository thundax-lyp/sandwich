package com.github.thundax.modules.auth.service;

import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.service.command.MemberAuthCommand;
import com.github.thundax.modules.auth.service.query.MemberAuthQuery;
import com.github.thundax.modules.auth.service.result.MemberTokenResult;

public interface MemberAuthService {

    MemberTokenResult loginAccount(MemberAuthCommand command);

    MemberTokenResult loginSms(MemberAuthCommand command);

    MemberTokenResult refreshAccessToken(MemberAuthCommand command);

    void logout(MemberAuthCommand command);

    PrincipalAccessToken getValidAccessToken(MemberAuthQuery query);

    void recordLoginFailed(MemberAuthCommand command);
}
