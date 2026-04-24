package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.UploadFileDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 上传文件 MyBatis Mapper。
 */
@MyBatisDao
public interface UploadFileMapper extends CrudDao<UploadFileDO> {

    UploadFileDO getContent(UploadFileDO uploadFile);

    List<UploadFileDO> findByFileIds(@Param("fileId") String[] fileId);
}
