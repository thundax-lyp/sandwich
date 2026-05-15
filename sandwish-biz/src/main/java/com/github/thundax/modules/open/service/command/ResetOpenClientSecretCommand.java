package com.github.thundax.modules.open.service.command;

import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetOpenClientSecretCommand {
    private OpenClientId id;
}
