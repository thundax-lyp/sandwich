package com.github.thundax.modules.auth.service;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.auth.entity.PrincipalAccessToken;
import com.github.thundax.modules.auth.service.command.MemberAuthCommand;
import com.github.thundax.modules.auth.service.query.MemberAuthQuery;
import com.github.thundax.modules.auth.service.result.MemberTokenResult;

public interface MemberAuthService {

    MemberTokenResult loginAccount(MemberAuthCommand command) throws ApiException;

    MemberTokenResult loginSms(MemberAuthCommand command) throws ApiException;

    MemberTokenResult refreshAccessToken(MemberAuthCommand command) throws ApiException;

    void logout(MemberAuthCommand command) throws ApiException;

    PrincipalAccessToken getValidAccessToken(MemberAuthQuery query);

    void recordLoginFailed(MemberAuthCommand command);
}
