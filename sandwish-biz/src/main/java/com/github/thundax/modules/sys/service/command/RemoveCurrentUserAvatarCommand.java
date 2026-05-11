package com.github.thundax.modules.sys.service.command;

import com.github.thundax.modules.sys.entity.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemoveCurrentUserAvatarCommand {

    private UserId userId;
}
