package com.github.thundax.modules.member.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.member.dao.MemberDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.entity.valueobject.MemberId;
import com.github.thundax.modules.member.entity.valueobject.MemberIdCodec;
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
    public Member get(MemberId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Member> list(MemberQuery query) {
        if (query != null && query.getIds() != null) {
            return dao.listByIds(MemberIdCodec.toValues(query.getIds()));
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
    @AuditLog(type = "Member", id = "", action = AuditAction.CREATE, summary = "创建会员", recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public MemberId create(MemberCommand command) {
        Member member = command.getMember();
        member.setId(dao.insert(member));
        return member.getId();
    }

    @Override
    @AuditLog(type = "Member", id = "#command.member.id.value()", action = AuditAction.UPDATE, summary = "更新会员")
    @Transactional(rollbackFor = Exception.class)
    public void change(MemberCommand command) {
        dao.update(command.getMember());
    }

    @Override
    @AuditLog(type = "Member", id = "#command.member.id.value()", action = AuditAction.UPDATE, summary = "更新会员信息")
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(MemberCommand command) {
        dao.updateInfo(command.getMember());
    }

    @Override
    @AuditLog(type = "Member", id = "#command.member.id.value()", action = AuditAction.UPDATE, summary = "更新会员状态")
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(MemberCommand command) {
        return dao.updateStatus(command.getMember());
    }

    @Override
    @AuditLog(
            type = "Member",
            id = "#command.id.value()",
            action = AuditAction.DELETE,
            summary = "删除会员",
            recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public int remove(MemberCommand command) {
        MemberId id = command.getId();
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
