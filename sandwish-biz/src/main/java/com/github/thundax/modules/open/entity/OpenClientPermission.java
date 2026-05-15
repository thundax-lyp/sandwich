package com.github.thundax.modules.open.entity;

import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import com.github.thundax.modules.open.entity.valueobject.OpenClientPermissionId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenClientPermission {
    private OpenClientPermissionId id;
    private OpenClientId clientId;
    private String permission;
}
