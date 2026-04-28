package com.github.thundax.modules.sys.utils;

import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.sys.service.UploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class UploadServiceHolder {

    private static UploadFileService service;

    @Autowired
    public UploadServiceHolder(UploadFileService targetService) {
        service = targetService;
    }

    public static UploadFileService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(UploadFileService.class);
        }
        return service;
    }
}
