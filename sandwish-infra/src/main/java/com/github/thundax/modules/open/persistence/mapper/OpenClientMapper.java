package com.github.thundax.modules.open.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.open.persistence.dataobject.OpenClientDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OpenClientMapper extends BaseMapper<OpenClientDO> {}
