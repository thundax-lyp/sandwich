package com.github.thundax.modules.assist.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.assist.persistence.dataobject.SignatureDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SignatureMapper extends BaseMapper<SignatureDO> {}
