package com.github.thundax.modules.member.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.ErrorCode;
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
import com.github.thundax.modules.member.service.command.MemberSortCommand;
import com.github.thundax.modules.member.service.query.MemberQuery;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private static final int PRIORITY_STEP = 10;

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
                query == null ? null : query.getRemarks(),
                query == null ? null : query.getSortDirection());
    }

    @Override
    public PageResult<Member> page(MemberQuery query, PageQuery page) {
        PageQuery normalizedPage = normalizePage(page);
        IPage<Member> dataPage = dao.page(
                query == null ? null : statusValue(query.getStatus()),
                query == null ? null : query.getName(),
                query == null ? null : query.getRemarks(),
                query == null ? null : query.getSortDirection(),
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
        member.setPriority(dao.maxPriority() + PRIORITY_STEP);
        member.setId(dao.insert(member));
        return member.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(MemberSortCommand command) throws ApiException {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<MemberId> orderedIdList = normalizeOrderedIds(command == null ? null : command.getOrderedIds());
        if (orderedIdList.isEmpty()) {
            throw new ApiException(ErrorCode.SORT_EMPTY_INPUT.getCode(), ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        List<Member> currentMembers = dao.list(null, null, null, effectiveDirection);
        if (currentMembers == null || currentMembers.isEmpty()) {
            throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
        }

        if (currentMembers.size() != orderedIdList.size()) {
            throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
        }

        Map<Long, Integer> indexById = new HashMap<>(currentMembers.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentMembers.size());
        List<MemberId> currentOrderedIds = new ArrayList<>(currentMembers.size());

        for (int i = 0; i < currentMembers.size(); i++) {
            Member member = currentMembers.get(i);
            if (member == null || member.getId() == null) {
                throw new ApiException(ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessage());
            }
            long memberId = member.getId().value();
            indexById.put(memberId, i);
            priorityById.put(memberId, member.getPriority());
            currentOrderedIds.add(member.getId());
        }

        for (MemberId orderedId : orderedIdList) {
            if (orderedId == null || !indexById.containsKey(orderedId.value())) {
                throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
            }
        }

        try {
            int temporaryPriority = dao.maxPriority() + PRIORITY_STEP;
            for (int i = 0; i < currentOrderedIds.size(); i++) {
                MemberId targetId = orderedIdList.get(i);
                MemberId currentId = currentOrderedIds.get(i);
                if (targetId.equals(currentId)) {
                    continue;
                }

                int targetIndex = indexById.get(targetId.value());
                int currentPriority = priorityById.get(currentId.value());
                int targetPriority = priorityById.get(targetId.value());

                updatePriorityOrThrow(targetId, temporaryPriority++, "暂态更新失败");
                updatePriorityOrThrow(currentId, targetPriority, "交换更新失败");
                updatePriorityOrThrow(targetId, currentPriority, "交换更新失败");

                priorityById.put(targetId.value(), currentPriority);
                priorityById.put(currentId.value(), targetPriority);

                currentOrderedIds.set(i, targetId);
                currentOrderedIds.set(targetIndex, currentId);
                indexById.put(targetId.value(), i);
                indexById.put(currentId.value(), targetIndex);
            }
        } catch (RuntimeException exception) {
            if (isConcurrentModification(exception)) {
                throw new ApiException(
                        ErrorCode.SORT_CONCURRENT_MODIFICATION.getCode(),
                        ErrorCode.SORT_CONCURRENT_MODIFICATION.getMessage());
            }
            throw new ApiException(ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessage());
        }
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

    private List<MemberId> normalizeOrderedIds(List<MemberId> orderedIds) throws ApiException {
        if (orderedIds == null) {
            return new ArrayList<>();
        }
        Set<Long> uniqueIdValues = new HashSet<>(orderedIds.size());
        List<MemberId> normalized = new ArrayList<>(orderedIds.size());
        for (MemberId orderedId : orderedIds) {
            if (orderedId == null || orderedId.value() == null) {
                throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
            }
            if (!uniqueIdValues.add(orderedId.value())) {
                throw new ApiException(ErrorCode.SORT_DUPLICATE_ID.getCode(), ErrorCode.SORT_DUPLICATE_ID.getMessage());
            }
            normalized.add(orderedId);
        }
        return normalized;
    }

    private boolean isConcurrentModification(RuntimeException exception) {
        Throwable cursor = exception;
        while (cursor != null) {
            if (cursor instanceof SQLException) {
                return isConcurrentSqlFailure((SQLException) cursor);
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    private boolean isConcurrentSqlFailure(SQLException sqlException) {
        int errorCode = sqlException.getErrorCode();
        String sqlState = sqlException.getSQLState();
        if (errorCode == 1205 || errorCode == 1213 || errorCode == 1207) {
            return true;
        }
        if (errorCode == 1222) {
            return true;
        }
        return "55P03".equals(sqlState)
                || "40P01".equals(sqlState)
                || "40001".equals(sqlState)
                || "23505".equals(sqlState);
    }

    private void updatePriorityOrThrow(MemberId id, int priority, String message) throws ApiException {
        int updated = dao.updatePriority(id, priority);
        if (updated != 1) {
            throw new ApiException(ErrorCode.SORT_DB_FAILURE.getCode(), message);
        }
    }
}
