package com.github.thundax.modules.auth.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.service.command.MemberRegistrationCommand;

public interface MemberRegistrationService {

    EntityId registerAccount(MemberRegistrationCommand command);

    void sendRegisterSmsCode(MemberRegistrationCommand command);

    EntityId registerMobile(MemberRegistrationCommand command);

    void sendRegisterEmailCode(MemberRegistrationCommand command);

    EntityId registerEmail(MemberRegistrationCommand command);
}
