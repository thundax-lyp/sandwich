package com.github.thundax.modules.member.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.service.MemberService;
import com.github.thundax.modules.member.service.command.MemberCommand;
import com.github.thundax.modules.member.service.query.MemberQuery;
import java.util.List;
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
    public Member get(MemberQuery query) {
        if (query == null || query.getId() == null) {
            return null;
        }
        return dao.getById(query.getId());
    }

    @Override
    public List<Member> list(MemberQuery query) {
        if (query != null && query.getIds() != null) {
            return dao.listByIds(EntityIdCodec.toValues(query.getIds()));
        }
        return dao.list(
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks());
    }

    @Override
    public PageResult<Member> page(MemberQuery query, PageQuery page) {
        PageQuery normalizedPage = normalizePage(page);
        IPage<Member> dataPage = dao.page(
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId create(MemberCommand command) {
        Member member = command.getMember();
        member.setId(dao.insert(member));
        return member.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void change(MemberCommand command) {
        dao.update(command.getMember());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(MemberCommand command) {
        dao.updateInfo(command.getMember());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(MemberCommand command) {
        return dao.updateStatus(command.getMember());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int remove(MemberCommand command) {
        EntityId id = command.getId();
        return id == null ? 0 : dao.deleteById(id);
    }

    private PageQuery normalizePage(PageQuery page) {
        PageQuery normalizedPage = page == null ? new PageQuery() : page;
        normalizedPage.normalize();
        return normalizedPage;
    }

    private String statusValue(MemberStatus status) {
        return status == null ? null : status.value();
    }
}
