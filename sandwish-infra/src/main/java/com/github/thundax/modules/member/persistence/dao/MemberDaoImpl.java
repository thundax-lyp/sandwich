package com.github.thundax.modules.member.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.valueobject.MemberId;
import com.github.thundax.modules.member.entity.valueobject.MemberIdCodec;
import com.github.thundax.modules.member.persistence.assembler.MemberPersistenceAssembler;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import com.github.thundax.modules.member.persistence.mapper.MemberMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class MemberDaoImpl implements MemberDao {

    private final MemberMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public MemberDaoImpl(MemberMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Member getById(MemberId id) {
        return MemberPersistenceAssembler.toEntity(mapper.selectById(id.value()));
    }

    @Override
    public List<Member> listByIds(List<Long> idList) {
        return MemberPersistenceAssembler.toEntityList(mapper.selectBatchIds(idList));
    }

    @Override
    public List<Member> list(String status, String name, String remarks, SortDirection sortDirection) {
        return MemberPersistenceAssembler.toEntityList(mapper.selectList(buildListWrapper(status, name, remarks, sortDirection)));
    }

    @Override
    public Page<Member> page(
            String status,
            String name,
            String remarks,
            SortDirection sortDirection,
            int pageNo,
            int pageSize) {
        Page<MemberDO> dataObjectPage =
                mapper.selectPage(
                        new Page<>(pageNo, pageSize),
                        buildListWrapper(status, name, remarks, sortDirection));
        Page<Member> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(MemberPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public int maxPriority() {
        Object max = mapper.selectObjs(new QueryWrapper<MemberDO>().select("max(priority)")).stream()
                .findFirst()
                .orElse(null);
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
    public MemberId insert(Member entity) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return MemberIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(Member entity) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(entity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberDO::getName, dataObject.getName())
                        .set(MemberDO::getGender, dataObject.getGender())
                        .set(MemberDO::getStatus, dataObject.getStatus())
                        .set(MemberDO::getRemarks, dataObject.getRemarks()));
    }

    @Override
    public int updatePriority(MemberId id, int priority) {
        return mapper.update(
                null,
                buildIdUpdateWrapper(id)
                        .set(MemberDO::getPriority, priority));
    }

    @Override
    public int deleteById(MemberId id) {
        return mapper.deleteById(id.value());
    }

    @Override
    public void updateInfo(Member member) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(member);
        mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberDO::getName, dataObject.getName())
                        .set(MemberDO::getGender, dataObject.getGender()));
    }

    @Override
    public int updateStatus(Member member) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(member);
        return mapper.update(null, buildIdUpdateWrapper(dataObject).set(MemberDO::getStatus, dataObject.getStatus()));
    }

    private LambdaUpdateWrapper<MemberDO> buildIdUpdateWrapper(MemberDO dataObject) {
        LambdaUpdateWrapper<MemberDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MemberDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaUpdateWrapper<MemberDO> buildIdUpdateWrapper(MemberId id) {
        LambdaUpdateWrapper<MemberDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MemberDO::getId, id.value());
        return wrapper;
    }

    private LambdaQueryWrapper<MemberDO> buildListWrapper(
            String status, String name, String remarks, SortDirection sortDirection) {
        LambdaQueryWrapper<MemberDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(MemberDO::getStatus, status);
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(MemberDO::getName, name);
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like(MemberDO::getRemarks, remarks);
        }
        if (SortDirection.DESC == sortDirection) {
            wrapper.orderByDesc(MemberDO::getPriority);
        } else {
            wrapper.orderByAsc(MemberDO::getPriority);
        }
        wrapper.orderByAsc(MemberDO::getId);
        return wrapper;
    }
}
