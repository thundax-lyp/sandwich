package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.sys.entity.UploadFile;
import java.util.List;

public interface UploadFileDao {

    UploadFile get(String id);

    List<UploadFile> getMany(List<String> idList);

    List<UploadFile> findList();

    Page<UploadFile> findPage(int pageNo, int pageSize);

    String insert(UploadFile uploadFile);

    int update(UploadFile uploadFile);

    int updatePriority(UploadFile uploadFile);

    int updateDelFlag(UploadFile uploadFile);

    int delete(String id);

    UploadFile getContent(String id);

    List<UploadFile> findByFileIds(List<String> fileIds);
}
