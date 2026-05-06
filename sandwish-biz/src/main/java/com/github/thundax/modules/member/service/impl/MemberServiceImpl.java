package com.github.thundax.modules.member.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.service.MemberService;
import com.github.thundax.modules.member.service.query.MemberQuery;
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
    public Member getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Member> listByIds(List<EntityId> ids) {
        return dao.listByIds(EntityIdCodec.toValues(ids));
    }

    @Override
    public List<Member> list(MemberQuery query) {
        return dao.list(
                query == null ? null : statusValue(query.getStatus()),
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
    public PageDTO<Member> page(MemberQuery query, PageDTO<Member> page) {
        PageDTO<Member> normalizedPage = normalizePage(page);
        IPage<Member> dataPage = dao.page(
                query == null ? null : statusValue(query.getStatus()),
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
        List<Member> members = dao.listByLoginName(loginName);
        return members == null || members.isEmpty() ? null : members.get(0);
    }

    @Override
    public Member getByEmail(String email) {
        List<Member> members = dao.listByEmail(email);
        return members == null || members.isEmpty() ? null : members.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId add(Member member) {
        member.setId(EntityIdCodec.toDomain(dao.insert(member)));
        return member.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Member member) {
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
    public int updateStatus(Member member) {
        return dao.updateStatus(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<Member> list) {
        return batchOperate(list, this::updateStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        return id == null ? 0 : dao.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member getByZjhm(MemberQuery query) {
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
        dao.updateLoginPass(member);
    }

    private <T> int batchOperate(Collection<T> collection, Function<T, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (T entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private PageDTO<Member> normalizePage(PageDTO<Member> page) {
        PageDTO<Member> normalizedPage = page == null ? new PageDTO<>() : page;
        normalizedPage.initialize();
        return normalizedPage;
    }

    private String statusValue(MemberStatus status) {
        return status == null ? null : status.value();
    }
}
