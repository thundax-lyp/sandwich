package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.UploadFileDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 上传文件 MyBatis Mapper。
 */
@MyBatisDao
public interface UploadFileMapper {

    UploadFileDO get(UploadFileDO uploadFile);

    List<UploadFileDO> getMany(@Param("idList") List<String> idList);

    List<UploadFileDO> findList(UploadFileDO uploadFile);

    int insert(UploadFileDO uploadFile);

    int update(UploadFileDO uploadFile);

    int updatePriority(UploadFileDO uploadFile);

    int updateStatus(UploadFileDO uploadFile);

    int updateDelFlag(UploadFileDO uploadFile);

    int delete(UploadFileDO uploadFile);

    UploadFileDO getContent(UploadFileDO uploadFile);

    List<UploadFileDO> findByFileIds(@Param("fileId") String[] fileId);
}
