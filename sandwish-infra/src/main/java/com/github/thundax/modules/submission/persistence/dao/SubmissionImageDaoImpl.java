package com.github.thundax.modules.submission.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.submission.dao.SubmissionImageDao;
import com.github.thundax.modules.submission.entity.SubmissionImage;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.persistence.assembler.SubmissionImagePersistenceAssembler;
import com.github.thundax.modules.submission.persistence.dataobject.SubmissionImageDO;
import com.github.thundax.modules.submission.persistence.mapper.SubmissionImageMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class SubmissionImageDaoImpl implements SubmissionImageDao {

    private final SubmissionImageMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public SubmissionImageDaoImpl(SubmissionImageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void batchInsert(List<SubmissionImage> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (SubmissionImage image : images) {
            SubmissionImageDO dataObject = SubmissionImagePersistenceAssembler.toDataObject(image);
            dataObject.setId(idGenerator.nextId().value());
            mapper.insert(dataObject);
        }
    }

    @Override
    public List<SubmissionImage> listBySubmissionId(SubmissionId submissionId) {
        LambdaQueryWrapper<SubmissionImageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubmissionImageDO::getSubmissionId, submissionId.value());
        wrapper.orderByAsc(SubmissionImageDO::getSortOrder);
        return SubmissionImagePersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public int deleteBySubmissionId(SubmissionId submissionId) {
        LambdaQueryWrapper<SubmissionImageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubmissionImageDO::getSubmissionId, submissionId.value());
        return mapper.delete(wrapper);
    }
}
