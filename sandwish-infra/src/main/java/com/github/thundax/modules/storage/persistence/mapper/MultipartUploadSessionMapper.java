package com.github.thundax.modules.storage.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.storage.persistence.dataobject.MultipartUploadSessionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MultipartUploadSessionMapper extends BaseMapper<MultipartUploadSessionDO> {}
