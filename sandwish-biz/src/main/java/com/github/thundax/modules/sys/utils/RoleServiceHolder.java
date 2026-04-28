package com.github.thundax.modules.sys.utils;

import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.service.RoleService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class RoleServiceHolder {

    private static RoleService service;

    private static final PooledThreadLocal<Map<String, Role>> ID_OBJECT_HOLDER = new PooledThreadLocal<>();

    @Autowired
    public RoleServiceHolder(RoleService targetService) {
        service = targetService;
    }

    public static RoleService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(RoleService.class);
        }
        return service;
    }

    public static Role get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return ID_OBJECT_HOLDER.computeIfAbsent(HashMap::new).computeIfAbsent(id, (key) -> getService()
                .get(id));
    }
}
