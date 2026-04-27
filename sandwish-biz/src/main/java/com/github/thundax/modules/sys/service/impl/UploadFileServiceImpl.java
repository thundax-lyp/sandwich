package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.modules.sys.dao.UploadFileDao;
import com.github.thundax.modules.sys.entity.UploadFile;
import com.github.thundax.modules.sys.service.UploadFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class UploadFileServiceImpl extends CrudServiceImpl<UploadFileDao, UploadFile> implements UploadFileService {

    public UploadFileServiceImpl(UploadFileDao dao) {
        super(dao);
    }

    @Override
    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void save(UploadFile entity){
        //保存记录
        super.save(entity);
    }

    @Override
    public UploadFile getContent(UploadFile uploadFile) {
        return dao.getContent(uploadFile);
    }

    @Override
    public List<UploadFile> findByFileIds(String[] fileId) {
        return dao.findByFileIds(fileId);
    }
}
