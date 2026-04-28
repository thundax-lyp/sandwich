package com.github.thundax.modules.sys.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {}
