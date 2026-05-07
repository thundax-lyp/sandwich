package com.github.thundax.modules.auth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.auth.persistence.dataobject.MemberRefreshTokenDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberRefreshTokenMapper extends BaseMapper<MemberRefreshTokenDO> {}
