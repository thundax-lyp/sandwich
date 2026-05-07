package com.github.thundax.modules.member.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.member.persistence.dataobject.MemberAuthSessionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberAuthSessionMapper extends BaseMapper<MemberAuthSessionDO> {}
