package com.github.thundax.modules.sys.utils;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.MenuService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class MenuServiceHolder {

    private static MenuService service;

    private static final PooledThreadLocal<Map<EntityId, Menu>> ID_OBJECT_HOLDER = new PooledThreadLocal<>();

    @Autowired
    public MenuServiceHolder(MenuService targetService) {
        service = targetService;
    }

    public static MenuService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(MenuService.class);
        }
        return service;
    }

    public static Menu get(EntityId id) {
        if (id == null) {
            return null;
        }
        return ID_OBJECT_HOLDER.computeIfAbsent(HashMap::new).computeIfAbsent(id, (key) -> getService()
                .get(id));
    }

    public static String getMenuIcon(String targetUrl) {
        Menu menu = getService().findList(User.MAX_RANKS).stream()
                .filter(item -> StringUtils.isNotEmpty(item.getUrl()) && targetUrl.endsWith(item.getUrl()))
                .findFirst()
                .orElse(null);
        if (menu != null) {
            return (String) menu.getDisplayParam("icon", StringUtils.EMPTY);
        }
        return StringUtils.EMPTY;
    }
}
