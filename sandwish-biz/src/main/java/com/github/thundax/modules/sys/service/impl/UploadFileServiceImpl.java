package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.dao.UploadFileDao;
import com.github.thundax.modules.sys.entity.UploadFile;
import com.github.thundax.modules.sys.service.UploadFileService;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UploadFileServiceImpl implements UploadFileService {

    private final UploadFileDao dao;

    public UploadFileServiceImpl(UploadFileDao dao) {
        this.dao = dao;
    }

    public UploadFile getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<UploadFile> listByIds(List<EntityId> ids) {
        return dao.listByIds(EntityIdCodec.toValues(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(UploadFile entity) {
        entity.setId(EntityIdCodec.toDomain(dao.insert(entity)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UploadFile entity) {
        dao.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        return id == null ? 0 : dao.deleteById(id);
    }

    @Override
    public UploadFile getContent(UploadFile uploadFile) {
        return uploadFile == null ? null : dao.getContentById(uploadFile.getId());
    }

    @Override
    public List<UploadFile> batchGetByFileIds(String[] fileId) {
        return dao.batchGetByFileIds(Arrays.asList(fileId));
    }
}
