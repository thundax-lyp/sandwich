package com.github.thundax.modules.member.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.persistence.assembler.MemberPersistenceAssembler;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import com.github.thundax.modules.member.persistence.mapper.MemberMapper;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

/** 会员 DAO 实现。 */
@Repository
public class MemberDaoImpl implements MemberDao {

    private final MemberMapper mapper;

    public MemberDaoImpl(MemberMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Member get(String id) {
        return MemberPersistenceAssembler.toEntity(mapper.selectById(id));
    }

    @Override
    public List<Member> getMany(List<String> idList) {
        return MemberPersistenceAssembler.toEntityList(mapper.selectBatchIds(idList));
    }

    @Override
    public List<Member> findList(
            String enableFlag,
            String email,
            String name,
            String remarks,
            Date beginRegisterDate,
            Date endRegisterDate,
            Date beginLoginDate,
            Date endLoginDate,
            String mobile) {
        return MemberPersistenceAssembler.toEntityList(
                mapper.selectList(
                        buildListWrapper(
                                enableFlag,
                                email,
                                name,
                                remarks,
                                beginRegisterDate,
                                endRegisterDate,
                                beginLoginDate,
                                endLoginDate,
                                mobile)));
    }

    @Override
    public Page<Member> findPage(
            String enableFlag,
            String email,
            String name,
            String remarks,
            Date beginRegisterDate,
            Date endRegisterDate,
            Date beginLoginDate,
            Date endLoginDate,
            String mobile,
            int pageNo,
            int pageSize) {
        Page<MemberDO> dataObjectPage =
                mapper.selectPage(
                        new Page<>(pageNo, pageSize),
                        buildListWrapper(
                                enableFlag,
                                email,
                                name,
                                remarks,
                                beginRegisterDate,
                                endRegisterDate,
                                beginLoginDate,
                                endLoginDate,
                                mobile));
        Page<Member> entityPage =
                new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(
                MemberPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public int insert(Member entity) {
        return mapper.insert(MemberPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(Member entity) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(entity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberDO::getName, dataObject.getName())
                        .set(MemberDO::getGender, dataObject.getGender())
                        .set(MemberDO::getMobile, dataObject.getMobile())
                        .set(MemberDO::getAddress, dataObject.getAddress())
                        .set(MemberDO::getEmail, dataObject.getEmail())
                        .set(MemberDO::getZipcode, dataObject.getZipcode())
                        .set(MemberDO::getEnableFlag, dataObject.getEnableFlag())
                        .set(MemberDO::getPriority, dataObject.getPriority())
                        .set(MemberDO::getRemarks, dataObject.getRemarks())
                        .set(MemberDO::getUpdateDate, dataObject.getUpdateDate()));
    }

    @Override
    public int updatePriority(Member entity) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(entity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberDO::getPriority, dataObject.getPriority())
                        .set(MemberDO::getUpdateDate, dataObject.getUpdateDate()));
    }

    @Override
    public int delete(String id) {
        return mapper.deleteById(id);
    }

    @Override
    public List<Member> findByLoginName(String loginName) {
        LambdaQueryWrapper<MemberDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberDO::getLoginName, loginName);
        return MemberPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public List<Member> findByEmail(String email) {
        LambdaQueryWrapper<MemberDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberDO::getEmail, email);
        return MemberPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public void updateLoginInfo(Member member) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(member);
        mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberDO::getLastLoginIp, dataObject.getLastLoginIp())
                        .set(MemberDO::getLastLoginDate, dataObject.getLastLoginDate())
                        .set(MemberDO::getLoginCount, dataObject.getLoginCount()));
    }

    @Override
    public void updateInfo(Member member) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(member);
        mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberDO::getName, dataObject.getName())
                        .set(MemberDO::getGender, dataObject.getGender())
                        .set(MemberDO::getMobile, dataObject.getMobile())
                        .set(MemberDO::getAddress, dataObject.getAddress())
                        .set(MemberDO::getZipcode, dataObject.getZipcode())
                        .set(MemberDO::getUpdateDate, dataObject.getUpdateDate()));
    }

    @Override
    public void updateLoginPass(Member member) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(member);
        mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberDO::getLoginPass, dataObject.getLoginPass()));
    }

    @Override
    public int updateEnableFlag(Member member) {
        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(member);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MemberDO::getEnableFlag, dataObject.getEnableFlag())
                        .set(MemberDO::getUpdateDate, dataObject.getUpdateDate()));
    }

    @Override
    public Member getByZjhm(String zjhm) {
        QueryWrapper<MemberDO> wrapper = new QueryWrapper<>();
        wrapper.eq("zjhm", zjhm);
        return MemberPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public Member getByYwtbId(String ywtbId) {
        LambdaQueryWrapper<MemberDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberDO::getYwtbId, ywtbId);
        return MemberPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    private LambdaUpdateWrapper<MemberDO> buildIdUpdateWrapper(MemberDO dataObject) {
        LambdaUpdateWrapper<MemberDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MemberDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaQueryWrapper<MemberDO> buildListWrapper(
            String enableFlag,
            String email,
            String name,
            String remarks,
            Date beginRegisterDate,
            Date endRegisterDate,
            Date beginLoginDate,
            Date endLoginDate,
            String mobile) {
        LambdaQueryWrapper<MemberDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberDO::getDelFlag, MemberDO.DEL_FLAG_NORMAL);
        if (StringUtils.isNotBlank(enableFlag)) {
            wrapper.eq(MemberDO::getEnableFlag, enableFlag);
        }
        if (StringUtils.isNotBlank(email)) {
            wrapper.like(MemberDO::getEmail, email);
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(MemberDO::getName, name);
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like(MemberDO::getRemarks, remarks);
        }
        if (beginRegisterDate != null && endRegisterDate != null) {
            wrapper.between(MemberDO::getRegisterDate, beginRegisterDate, endRegisterDate);
        }
        if (beginLoginDate != null && endLoginDate != null) {
            wrapper.between(MemberDO::getLastLoginDate, beginLoginDate, endLoginDate);
        }
        if (StringUtils.isNotBlank(mobile)) {
            wrapper.eq(MemberDO::getMobile, mobile);
        }
        wrapper.orderByAsc(MemberDO::getPriority, MemberDO::getEmail);
        return wrapper;
    }
}
