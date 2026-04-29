package com.github.thundax.modules.storage.utils;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.service.StorageService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class StorageServiceHolder {

    private static StorageService service;

    private static final PooledThreadLocal<Map<EntityId, Storage>> ID_OBJECT_HOLDER = new PooledThreadLocal<>();

    public static StorageService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(StorageService.class);
        }
        return service;
    }

    public static Storage get(EntityId id) {
        if (id == null) {
            return null;
        }
        return ID_OBJECT_HOLDER.computeIfAbsent(HashMap::new).computeIfAbsent(id, (key) -> getService()
                .get(id));
    }
}
