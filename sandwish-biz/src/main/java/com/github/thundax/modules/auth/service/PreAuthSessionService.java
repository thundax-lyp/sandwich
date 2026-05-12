package com.github.thundax.modules.auth.service;

import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.service.command.CreatePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.RefreshPreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.ReleasePreAuthSessionCommand;
import com.github.thundax.modules.auth.service.command.UpsertPreAuthSessionValueCommand;
import com.github.thundax.modules.auth.service.query.PreAuthSessionValueQuery;

public interface PreAuthSessionService {

    int countActiveSessions();

    PreAuthSession create(CreatePreAuthSessionCommand command);

    PreAuthSessionId getIdByToken(PreAuthSessionToken token);

    PreAuthSessionId getIdByRefreshToken(PreAuthSessionToken refreshToken);

    PreAuthSession get(PreAuthSessionId id);

    PreAuthSession refresh(RefreshPreAuthSessionCommand command);

    void release(ReleasePreAuthSessionCommand command);

    void upsertValue(UpsertPreAuthSessionValueCommand command);

    String getValue(PreAuthSessionValueQuery query);
}
