package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.UploadFile;
import java.util.List;

public interface UploadFileService {

    UploadFile getById(EntityId id);

    List<UploadFile> batchGetByIds(List<EntityId> ids);

    void add(UploadFile uploadFile);

    void update(UploadFile uploadFile);

    int deleteById(EntityId id);

    UploadFile getContent(UploadFile uploadFile);

    List<UploadFile> batchGetByFileIds(String[] fileId);
}
