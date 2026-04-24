package com.github.thundax.modules.sys.utils;

import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.service.LogService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author wdit
 */
@Service
@Lazy(value = false)
public class LogServiceHolder {

    private static LogService service;

    public LogServiceHolder(LogService targetService) {
        service = targetService;
    }

    public static LogService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(LogService.class);
        }
        return service;
    }

    public static Log get(String id) {
        return getService().get(id);
    }
}
