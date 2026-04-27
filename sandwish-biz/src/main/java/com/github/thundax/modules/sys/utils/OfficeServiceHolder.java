package com.github.thundax.modules.sys.utils;

import com.github.thundax.common.collect.MapUtils;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.service.OfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author wdit
 */
@Service
@Lazy(false)
public class OfficeServiceHolder {

    private static OfficeService service;

    private static final PooledThreadLocal<Map<String, Office>> ID_OBJECT_HOLDER = new PooledThreadLocal<>();

    @Autowired
    public OfficeServiceHolder(OfficeService targetService) {
        service = targetService;
    }

    public static OfficeService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(OfficeService.class);
        }
        return service;
    }

    public static Office get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return ID_OBJECT_HOLDER
                .computeIfAbsent(MapUtils::newHashMap)
                .computeIfAbsent(id, (key) -> getService().get(id));
    }

}
