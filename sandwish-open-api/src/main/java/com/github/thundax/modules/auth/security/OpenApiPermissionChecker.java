package com.github.thundax.modules.auth.security;

import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import com.github.thundax.modules.open.service.OpenClientService;
import com.github.thundax.modules.open.service.dto.OpenClientDTO;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class OpenApiPermissionChecker {

    private final OpenClientService openClientService;

    public OpenApiPermissionChecker(OpenClientService openClientService) {
        this.openClientService = openClientService;
    }

    public Set<String> permissions(OpenClientId clientId) {
        OpenClientDTO client = openClientService.get(clientId);
        if (client == null || client.getPermissions() == null) {
            return Collections.emptySet();
        }
        Set<String> permissions = new LinkedHashSet<>();
        for (String permission : client.getPermissions()) {
            if (StringUtils.isNotBlank(permission)) {
                permissions.add(permission.trim());
            }
        }
        return permissions;
    }
}
