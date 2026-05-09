package com.github.thundax.modules.audit.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.audit.persistence.dataobject.AuditMetaDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditMetaMapper extends BaseMapper<AuditMetaDO> {}
