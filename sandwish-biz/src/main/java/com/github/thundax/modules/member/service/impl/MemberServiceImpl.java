package com.github.thundax.modules.member.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.service.MemberService;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberDao dao;

    @Autowired
    public MemberServiceImpl(MemberDao dao) {
        this.dao = dao;
    }

    @Override
    public Member get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return dao.get(id);
    }

    @Override
    public List<Member> getMany(List<String> ids) {
        return dao.getMany(ids);
    }

    @Override
    public List<Member> findList(Member member) {
        Member.Query query = member == null ? null : member.getQuery();
        return dao.findList(
                query == null ? null : query.getEnableFlag(),
                query == null ? null : query.getEmail(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks(),
                query == null ? null : query.getBeginRegisterDate(),
                query == null ? null : query.getEndRegisterDate(),
                query == null ? null : query.getBeginLoginDate(),
                query == null ? null : query.getEndLoginDate(),
                query == null ? null : query.getMobile());
    }

    @Override
    public Page<Member> findPage(Member member, Page<Member> page) {
        Page<Member> normalizedPage = normalizePage(page);
        Member.Query query = member == null ? null : member.getQuery();
        IPage<Member> dataPage = dao.findPage(
                query == null ? null : query.getEnableFlag(),
                query == null ? null : query.getEmail(),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks(),
                query == null ? null : query.getBeginRegisterDate(),
                query == null ? null : query.getEndRegisterDate(),
                query == null ? null : query.getBeginLoginDate(),
                query == null ? null : query.getEndLoginDate(),
                query == null ? null : query.getMobile(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    public Member getByLoginName(String loginName) {
        List<Member> members = dao.findByLoginName(loginName);
        return members == null || members.isEmpty() ? null : members.get(0);
    }

    @Override
    public Member getByEmail(String email) {
        List<Member> members = dao.findByEmail(email);
        return members == null || members.isEmpty() ? null : members.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Member member) {
        member.preInsert();
        member.setId(dao.insert(member));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Member member) {
        member.preUpdate();
        dao.update(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(Member member) {
        dao.updateLoginInfo(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(Member member) {
        dao.updateInfo(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(Member member) {
        return dao.updateEnableFlag(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(List<Member> list) {
        return batchOperate(list, this::updateEnableFlag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(Member member) {
        member.preUpdate();
        return dao.updatePriority(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<Member> list) {
        return batchOperate(list, this::updatePriority);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Member member) {
        if (member == null) {
            return 0;
        }
        return dao.delete(member.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(List<Member> list) {
        return batchOperate(list, this::delete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member getByZjhm(Member member) {
        Member.Query query = member == null ? null : member.getQuery();
        return dao.getByZjhm(query == null ? null : query.getZjhm());
    }

    @Override
    public Member getByYwtbId(String ywtbUserId) {
        if (StringUtils.isEmpty(ywtbUserId)) {
            return null;
        }
        return dao.getByYwtbId(ywtbUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Member member) {
        member.preUpdate();
        dao.updateLoginPass(member);
    }

    private int batchOperate(Collection<Member> collection, Function<Member, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (Member entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private Page<Member> normalizePage(Page<Member> page) {
        Page<Member> normalizedPage = page == null ? new Page<>() : page;
        normalizedPage.initialize();
        return normalizedPage;
    }
}
