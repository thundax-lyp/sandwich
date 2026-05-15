package com.github.thundax.modules.submission.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.modules.submission.persistence.dataobject.SubmissionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubmissionMapper extends BaseMapper<SubmissionDO> {}
