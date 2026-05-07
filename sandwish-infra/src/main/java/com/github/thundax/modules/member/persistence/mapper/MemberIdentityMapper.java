package com.github.thundax.modules.member.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.member.persistence.dataobject.MemberIdentityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberIdentityMapper extends BaseMapper<MemberIdentityDO> {}
