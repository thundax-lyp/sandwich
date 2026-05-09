package com.github.thundax.modules.auth.service.query;

import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreAuthSessionQuery {
    private PreAuthSessionId id;
    private PreAuthSessionToken token;
    private PreAuthSessionToken refreshToken;
    private String name;
}
