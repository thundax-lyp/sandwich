package com.github.thundax.modules.auth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.auth.persistence.dataobject.PrincipalIdentityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrincipalIdentityMapper extends BaseMapper<PrincipalIdentityDO> {}
