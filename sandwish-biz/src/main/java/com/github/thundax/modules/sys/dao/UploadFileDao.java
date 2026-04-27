package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.modules.sys.entity.UploadFile;
import java.util.List;

/** @author wdit */
public interface UploadFileDao extends CrudDao<UploadFile> {

    UploadFile getContent(UploadFile uploadFile);

    List<UploadFile> findByFileIds(String[] fileId);
}
