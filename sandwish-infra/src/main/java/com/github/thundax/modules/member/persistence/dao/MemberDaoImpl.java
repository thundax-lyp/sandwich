package com.github.thundax.modules.member.persistence.dao;

import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.persistence.assembler.MemberPersistenceAssembler;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import com.github.thundax.modules.member.persistence.mapper.MemberMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会员 DAO 实现。
 */
@Repository
public class MemberDaoImpl implements MemberDao {

    private final MemberMapper mapper;

    public MemberDaoImpl(MemberMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Member get(Member entity) {
        return MemberPersistenceAssembler.toEntity(mapper.get(MemberPersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<Member> getMany(List<String> idList) {
        return MemberPersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Member> findList(Member entity) {
        List<MemberDO> dataObjects = mapper.findList(MemberPersistenceAssembler.toDataObject(entity));
        List<Member> entities = MemberPersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Member entity) {
        return mapper.insert(MemberPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(Member entity) {
        return mapper.update(MemberPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(Member entity) {
        return mapper.updatePriority(MemberPersistenceAssembler.toDataObject(entity));
    }

    public int updateStatus(Member entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(Member entity) {
        return 0;
    }

    @Override
    public int delete(Member entity) {
        return mapper.delete(MemberPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public List<Member> findByLoginName(String loginName) {
        return MemberPersistenceAssembler.toEntityList(mapper.findByLoginName(loginName));
    }

    @Override
    public List<Member> findByEmail(String email) {
        return MemberPersistenceAssembler.toEntityList(mapper.findByEmail(email));
    }

    @Override
    public void updateLoginInfo(Member member) {
        mapper.updateLoginInfo(MemberPersistenceAssembler.toDataObject(member));
    }

    @Override
    public void updateInfo(Member member) {
        mapper.updateInfo(MemberPersistenceAssembler.toDataObject(member));
    }

    @Override
    public void updateLoginPass(Member member) {
        mapper.updateLoginPass(MemberPersistenceAssembler.toDataObject(member));
    }

    @Override
    public int updateEnableFlag(Member member) {
        return mapper.updateEnableFlag(MemberPersistenceAssembler.toDataObject(member));
    }

    @Override
    public Member getByZjhm(Member member) {
        return MemberPersistenceAssembler.toEntity(mapper.getByZjhm(MemberPersistenceAssembler.toDataObject(member)));
    }

    @Override
    public Member getByYwtbId(Member member) {
        return MemberPersistenceAssembler.toEntity(mapper.getByYwtbId(MemberPersistenceAssembler.toDataObject(member)));
    }
}
