package com.github.thundax.modules.auth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.auth.persistence.dataobject.UserCredentialDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCredentialMapper extends BaseMapper<UserCredentialDO> {}
