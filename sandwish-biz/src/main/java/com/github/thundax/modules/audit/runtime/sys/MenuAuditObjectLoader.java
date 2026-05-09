package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.sys.entity.valueobject.MenuId;
import com.github.thundax.modules.sys.service.MenuService;
import org.springframework.stereotype.Component;

@Component
public class MenuAuditObjectLoader implements AuditObjectLoader {

    private final MenuService menuService;

    public MenuAuditObjectLoader(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public String objectType() {
        return "Menu";
    }

    @Override
    public Object load(String objectId) {
        return menuService.get(MenuId.of(Long.valueOf(objectId)));
    }
}
