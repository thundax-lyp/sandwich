package com.github.thundax.modules.auth.service.command;

import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpsertPreAuthSessionValueCommand {
    private PreAuthSessionId id;
    private String name;
    private String value;
    private long expiredAt;
}
