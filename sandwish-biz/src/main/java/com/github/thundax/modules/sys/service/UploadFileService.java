package com.github.thundax.modules.sys.service;

import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.UploadFile;
import java.util.List;

/** @author wdit */
public interface UploadFileService extends CrudService<UploadFile> {

    UploadFile getContent(UploadFile uploadFile);

    List<UploadFile> findByFileIds(String[] fileId);
}
