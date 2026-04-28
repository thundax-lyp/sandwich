package com.github.thundax.modules.sys.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.sys.persistence.dataobject.UserEncryptDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserEncryptMapper extends BaseMapper<UserEncryptDO> {}
