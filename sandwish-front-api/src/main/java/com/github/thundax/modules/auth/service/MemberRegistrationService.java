package com.github.thundax.modules.auth.service;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.service.command.MemberRegistrationCommand;

public interface MemberRegistrationService {

    EntityId registerAccount(MemberRegistrationCommand command) throws ApiException;

    void sendRegisterSmsCode(MemberRegistrationCommand command) throws ApiException;

    EntityId registerMobile(MemberRegistrationCommand command) throws ApiException;

    void sendRegisterEmailCode(MemberRegistrationCommand command) throws ApiException;

    EntityId registerEmail(MemberRegistrationCommand command) throws ApiException;
}
