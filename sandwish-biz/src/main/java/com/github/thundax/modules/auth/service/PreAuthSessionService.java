package com.github.thundax.modules.auth.service;

import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.service.command.CreatePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.RefreshPreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.ReleasePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.github.thundax.modules.auth.service.query.PreAuthSessionQuery;

public interface PreAuthSessionService {

    int count(PreAuthSessionQuery query);

    PreAuthSession create(CreatePreAuthSessionCommand command);

    PreAuthSessionId getIdByToken(PreAuthSessionQuery query);

    PreAuthSessionId getIdByRefreshToken(PreAuthSessionQuery query);

    PreAuthSession get(PreAuthSessionQuery query) throws InvalidTokenException;

    PreAuthSession refresh(RefreshPreAuthSessionCommand command) throws InvalidTokenException;

    void release(ReleasePreAuthSessionCommand command);

    void upsertValue(UpsertPreAuthSessionValueCommand command) throws InvalidTokenException;

    String getValue(PreAuthSessionQuery query) throws InvalidTokenException;
}
