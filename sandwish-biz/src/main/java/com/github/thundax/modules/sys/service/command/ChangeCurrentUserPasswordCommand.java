package com.github.thundax.modules.sys.service.command;

import com.github.thundax.common.id.EntityId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCurrentUserPasswordCommand {
    private EntityId userId;
    private String oldPassword;
    private String password;
}
