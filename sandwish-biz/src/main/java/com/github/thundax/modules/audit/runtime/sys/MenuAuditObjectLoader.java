package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.query.MenuQuery;
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
        MenuQuery query = new MenuQuery();
        query.setId(EntityIdCodec.toDomain(Long.valueOf(objectId)));
        return menuService.get(query);
    }
}
