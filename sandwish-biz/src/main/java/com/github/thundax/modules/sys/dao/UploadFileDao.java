package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.UploadFile;
import java.util.List;

public interface UploadFileDao {

    UploadFile getById(EntityId id);

    List<UploadFile> listByIds(List<String> idList);

    List<UploadFile> list();

    Page<UploadFile> page(int pageNo, int pageSize);

    String insert(UploadFile uploadFile);

    int update(UploadFile uploadFile);

    int updatePriority(UploadFile uploadFile);

    int deleteById(EntityId id);

    UploadFile getContentById(EntityId id);

    List<UploadFile> batchGetByFileIds(List<String> fileIds);
}
