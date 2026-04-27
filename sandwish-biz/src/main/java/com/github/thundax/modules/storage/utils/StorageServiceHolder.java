package com.github.thundax.modules.storage.utils;

import com.github.thundax.common.collect.MapUtils;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.service.StorageService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author wdit
 */
@Service
@Lazy(false)
public class StorageServiceHolder {

    private static StorageService service;

    private static final PooledThreadLocal<Map<String, Storage>> ID_OBJECT_HOLDER = new PooledThreadLocal<>();


    public static StorageService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(StorageService.class);
        }
        return service;
    }

    public static Storage get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return ID_OBJECT_HOLDER
                .computeIfAbsent(MapUtils::newHashMap)
                .computeIfAbsent(id, (key) -> getService().get(id));
    }

}
