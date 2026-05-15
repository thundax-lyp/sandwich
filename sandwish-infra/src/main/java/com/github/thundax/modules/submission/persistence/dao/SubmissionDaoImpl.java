package com.github.thundax.modules.submission.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.submission.dao.SubmissionDao;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionIdCodec;
import com.github.thundax.modules.submission.persistence.assembler.SubmissionPersistenceAssembler;
import com.github.thundax.modules.submission.persistence.dataobject.SubmissionDO;
import com.github.thundax.modules.submission.persistence.mapper.SubmissionMapper;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class SubmissionDaoImpl implements SubmissionDao {

    private final SubmissionMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public SubmissionDaoImpl(SubmissionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Submission getById(SubmissionId id) {
        return SubmissionPersistenceAssembler.toEntity(mapper.selectById(id.value()));
    }

    @Override
    public List<Submission> listByIds(List<Long> idList) {
        return SubmissionPersistenceAssembler.toEntityList(mapper.selectBatchIds(idList));
    }

    @Override
    public List<Submission> list(
            String status,
            String sourceClientId,
            Date submittedAtBegin,
            Date submittedAtEnd,
            SortDirection sortDirection) {
        return SubmissionPersistenceAssembler.toEntityList(mapper.selectList(
                buildListWrapper(status, sourceClientId, submittedAtBegin, submittedAtEnd, sortDirection)));
    }

    @Override
    public Page<Submission> page(
            String status,
            String sourceClientId,
            Date submittedAtBegin,
            Date submittedAtEnd,
            SortDirection sortDirection,
            int pageNo,
            int pageSize) {
        Page<SubmissionDO> dataObjectPage = mapper.selectPage(
                new Page<>(pageNo, pageSize),
                buildListWrapper(status, sourceClientId, submittedAtBegin, submittedAtEnd, sortDirection));
        Page<Submission> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(SubmissionPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public int maxPriority() {
        List<Object> maxValues = mapper.selectObjs(new QueryWrapper<SubmissionDO>().select("max(priority)"));
        Object max = null;
        for (Object value : maxValues) {
            if (value != null) {
                max = value;
                break;
            }
        }
        if (max == null) {
            return 0;
        }
        if (max instanceof Number) {
            return ((Number) max).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(max));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    @Override
    public SubmissionId insert(Submission entity) {
        SubmissionDO dataObject = SubmissionPersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return SubmissionIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateStatus(Submission entity) {
        SubmissionDO dataObject = SubmissionPersistenceAssembler.toDataObject(entity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(SubmissionDO::getStatus, dataObject.getStatus())
                        .set(SubmissionDO::getLastStatusChangedAt, dataObject.getLastStatusChangedAt()));
    }

    @Override
    public int updatePriority(SubmissionId id, int priority) {
        return mapper.update(null, buildIdUpdateWrapper(id).set(SubmissionDO::getPriority, priority));
    }

    @Override
    public int deleteById(SubmissionId id) {
        return mapper.deleteById(id.value());
    }

    private LambdaUpdateWrapper<SubmissionDO> buildIdUpdateWrapper(SubmissionDO dataObject) {
        LambdaUpdateWrapper<SubmissionDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SubmissionDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaUpdateWrapper<SubmissionDO> buildIdUpdateWrapper(SubmissionId id) {
        LambdaUpdateWrapper<SubmissionDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SubmissionDO::getId, id.value());
        return wrapper;
    }

    private LambdaQueryWrapper<SubmissionDO> buildListWrapper(
            String status,
            String sourceClientId,
            Date submittedAtBegin,
            Date submittedAtEnd,
            SortDirection sortDirection) {
        LambdaQueryWrapper<SubmissionDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(SubmissionDO::getStatus, status);
        }
        if (StringUtils.isNotBlank(sourceClientId)) {
            wrapper.eq(SubmissionDO::getSourceClientId, sourceClientId);
        }
        if (submittedAtBegin != null) {
            wrapper.ge(SubmissionDO::getSubmittedAt, submittedAtBegin);
        }
        if (submittedAtEnd != null) {
            wrapper.le(SubmissionDO::getSubmittedAt, submittedAtEnd);
        }
        if (SortDirection.DESC == sortDirection) {
            wrapper.orderByDesc(SubmissionDO::getPriority);
        } else {
            wrapper.orderByAsc(SubmissionDO::getPriority);
        }
        wrapper.orderByAsc(SubmissionDO::getId);
        return wrapper;
    }
}
