package com.github.thundax.modules.member.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.persistence.assembler.MemberPersistenceAssembler;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import com.github.thundax.modules.member.persistence.mapper.MemberMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class MemberDaoImpl implements MemberDao {

    private static final String DEL_FLAG_COLUMN = "del_flag";
    private static final String NORMAL_DEL_FLAG = "0";

    private final MemberMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public MemberDaoImpl(MemberMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Member getById(EntityId id) {
        return MemberPersistenceAssembler.toEntity(mapper.selectById(id.value()));
    }

    @Override
    public List<Member> listByIds(List<Long> idList) {
        return MemberPersistenceAssembler.toEntityList(mapper.selectBatchIds(idList));
    }

    @Override
    public List<Member> list(String status, String name, String remarks) {
        return MemberPersistenceAssembler.toEntityList(mapper.selectList(buildListWrapper(status, name, remarks)));
    }

    @Override
    public Page<Member> page(String status, String name, String remarks, int pageNo, int pageSize) {
        Page<MemberDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper(status, name, remarks));
        Page<Member> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(MemberPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public EntityId insert(Member entity) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        mapper.update(
                null,
                new UpdateWrapper<MemberDO>()
                        .set(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG)
                        .eq("id", dataObject.getId()));
        return EntityIdCodec.toDomain(dataObject.getId());
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
                        .set(MemberDO::getPriority, dataObject.getPriority())
                        .set(MemberDO::getRemarks, dataObject.getRemarks()));
    }

    @Override
    public int updatePriority(Member entity) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(entity);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(MemberDO::getPriority, dataObject.getPriority()));
    }

    @Override
    public int deleteById(EntityId id) {
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

    private LambdaQueryWrapper<MemberDO> buildListWrapper(String status, String name, String remarks) {
        LambdaQueryWrapper<MemberDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("del_flag = {0}", NORMAL_DEL_FLAG);
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(MemberDO::getStatus, status);
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(MemberDO::getName, name);
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like(MemberDO::getRemarks, remarks);
        }
        wrapper.orderByAsc(MemberDO::getPriority, MemberDO::getName);
        return wrapper;
    }
}
