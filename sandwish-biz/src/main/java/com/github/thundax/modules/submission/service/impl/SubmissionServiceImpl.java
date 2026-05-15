package com.github.thundax.modules.submission.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.BizException;
import com.github.thundax.common.exception.BizExceptionBoundary;
import com.github.thundax.common.exception.ErrorCode;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.command.AddStorageReferencesCommand;
import com.github.thundax.modules.storage.service.command.RemoveStorageReferencesCommand;
import com.github.thundax.modules.submission.dao.SubmissionDao;
import com.github.thundax.modules.submission.dao.SubmissionImageDao;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.SubmissionImage;
import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionIdCodec;
import com.github.thundax.modules.submission.service.SubmissionService;
import com.github.thundax.modules.submission.service.command.ChangeSubmissionStatusCommand;
import com.github.thundax.modules.submission.service.command.CreateSubmissionCommand;
import com.github.thundax.modules.submission.service.command.SubmissionSortCommand;
import com.github.thundax.modules.submission.service.query.SubmissionQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class SubmissionServiceImpl implements SubmissionService {

    private static final int PRIORITY_STEP = 10;

    private final SubmissionDao submissionDao;
    private final SubmissionImageDao submissionImageDao;
    private final StorageService storageService;

    public SubmissionServiceImpl(
            SubmissionDao submissionDao, SubmissionImageDao submissionImageDao, StorageService storageService) {
        this.submissionDao = submissionDao;
        this.submissionImageDao = submissionImageDao;
        this.storageService = storageService;
    }

    @Override
    public Submission get(SubmissionId id) {
        if (id == null) {
            return null;
        }
        Submission submission = submissionDao.getById(id);
        fillImages(submission);
        return submission;
    }

    @Override
    public List<Submission> list(SubmissionQuery query) {
        if (query != null && query.getIds() != null) {
            return submissionDao.listByIds(SubmissionIdCodec.toValues(query.getIds()));
        }
        return submissionDao.list(
                statusValue(query),
                query == null ? null : query.getSourceClientId(),
                query == null ? null : query.getSubmittedAtBegin(),
                query == null ? null : query.getSubmittedAtEnd(),
                query == null ? null : query.getSortDirection());
    }

    @Override
    public PageResult<Submission> page(SubmissionQuery query, PageQuery page) {
        IPage<Submission> dataPage = submissionDao.page(
                statusValue(query),
                query == null ? null : query.getSourceClientId(),
                query == null ? null : query.getSubmittedAtBegin(),
                query == null ? null : query.getSubmittedAtEnd(),
                query == null ? null : query.getSortDirection(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @AuditLog(type = "Submission", id = "", action = AuditAction.CREATE, summary = "创建提交内容", recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public SubmissionId create(CreateSubmissionCommand command) {
        validateCreate(command);
        Submission submission = new Submission();
        submission.setTitle(command.getTitle());
        submission.setContent(command.getContent());
        submission.setSourceClientId(command.getSourceClientId());
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setPriority(submissionDao.maxPriority() + PRIORITY_STEP);
        submission.setSubmittedAt(new Date());
        submission.setId(submissionDao.insert(submission));
        submissionImageDao.batchInsert(toImages(submission.getId(), command.getImageObjectIds()));
        storageService.addReferences(
                new AddStorageReferencesCommand(toStorageReferences(submission.getId(), command.getImageObjectIds())));
        return submission.getId();
    }

    @Override
    @AuditLog(
            type = "Submission",
            id = "#id.value()",
            action = AuditAction.DELETE,
            summary = "删除提交内容",
            recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public int remove(SubmissionId id) {
        if (id == null) {
            return 0;
        }
        Submission submission = get(id);
        if (submission == null) {
            return 0;
        }
        String ownerId = String.valueOf(id.value());
        storageService.removeReferences(new RemoveStorageReferencesCommand(StorageOwnerType.SUBMISSION, ownerId));
        submissionImageDao.deleteBySubmissionId(id);
        return submissionDao.deleteById(id);
    }

    @Override
    @AuditLog(type = "Submission", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "更新提交内容状态")
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(ChangeSubmissionStatusCommand command) {
        if (command == null || command.getId() == null || command.getStatus() == null) {
            throw invalidParameter("提交内容状态参数无效");
        }
        Submission submission = new Submission();
        submission.setId(command.getId());
        submission.setStatus(command.getStatus());
        submission.setLastStatusChangedAt(new Date());
        return submissionDao.updateStatus(submission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(SubmissionSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<SubmissionId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_EMPTY_INPUT.getCode(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        List<Submission> currentSubmissions = submissionDao.list(null, null, null, null, effectiveDirection);
        if (currentSubmissions == null || currentSubmissions.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        if (currentSubmissions.size() != orderedIdList.size()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        sortByExchange(orderedIdList, currentSubmissions);
    }

    private void sortByExchange(List<SubmissionId> orderedIdList, List<Submission> currentSubmissions) {
        Map<Long, Integer> indexById = new HashMap<>(currentSubmissions.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentSubmissions.size());
        List<SubmissionId> currentOrderedIds = new ArrayList<>(currentSubmissions.size());

        for (int i = 0; i < currentSubmissions.size(); i++) {
            Submission submission = currentSubmissions.get(i);
            if (submission == null || submission.getId() == null) {
                throw new BizException(
                        ErrorCode.SORT_DB_FAILURE.getCode(),
                        ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                        ErrorCode.SORT_DB_FAILURE.getMessage());
            }
            long submissionId = submission.getId().value();
            indexById.put(submissionId, i);
            priorityById.put(submissionId, submission.getPriority());
            currentOrderedIds.add(submission.getId());
        }

        assertSortInput(orderedIdList, indexById);
        exchangePriorities(orderedIdList, indexById, priorityById, currentOrderedIds);
    }

    private void exchangePriorities(
            List<SubmissionId> orderedIdList,
            Map<Long, Integer> indexById,
            Map<Long, Integer> priorityById,
            List<SubmissionId> currentOrderedIds) {
        int temporaryPriority = submissionDao.maxPriority() + PRIORITY_STEP;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            SubmissionId targetId = orderedIdList.get(i);
            SubmissionId currentId = currentOrderedIds.get(i);
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
    }

    private void assertSortInput(List<SubmissionId> orderedIdList, Map<Long, Integer> indexById) {
        Set<Long> seen = new LinkedHashSet<>();
        for (SubmissionId orderedId : orderedIdList) {
            if (orderedId == null || !indexById.containsKey(orderedId.value())) {
                throw new BizException(
                        ErrorCode.SORT_MISSING_ID.getCode(),
                        ErrorCode.SORT_MISSING_ID.getMessageKey(),
                        ErrorCode.SORT_MISSING_ID.getMessage());
            }
            if (!seen.add(orderedId.value())) {
                throw new BizException(
                        ErrorCode.SORT_DUPLICATE_ID.getCode(),
                        ErrorCode.SORT_DUPLICATE_ID.getMessageKey(),
                        ErrorCode.SORT_DUPLICATE_ID.getMessage());
            }
        }
    }

    private void updatePriorityOrThrow(SubmissionId id, int priority, String message) {
        int updated = submissionDao.updatePriority(id, priority);
        if (updated != 1) {
            throw new BizException(
                    ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessageKey(), message);
        }
    }

    private void fillImages(Submission submission) {
        if (submission == null || submission.getId() == null) {
            return;
        }
        submission.setImages(submissionImageDao.listBySubmissionId(submission.getId()));
    }

    private static String statusValue(SubmissionQuery query) {
        return query == null || query.getStatus() == null
                ? null
                : query.getStatus().value();
    }

    private static List<SubmissionImage> toImages(SubmissionId submissionId, List<StoredObjectId> imageObjectIds) {
        List<SubmissionImage> images = new ArrayList<>();
        for (int i = 0; i < imageObjectIds.size(); i++) {
            SubmissionImage image = new SubmissionImage();
            image.setSubmissionId(submissionId);
            image.setStorageObjectId(imageObjectIds.get(i));
            image.setSortOrder(i);
            images.add(image);
        }
        return images;
    }

    private static List<StoredObjectReference> toStorageReferences(
            SubmissionId submissionId, List<StoredObjectId> imageObjectIds) {
        List<StoredObjectReference> references = new ArrayList<>();
        for (StoredObjectId imageObjectId : imageObjectIds) {
            StoredObjectReference reference = new StoredObjectReference();
            reference.setObjectId(imageObjectId);
            reference.setOwnerType(StorageOwnerType.SUBMISSION);
            reference.setOwnerId(String.valueOf(submissionId.value()));
            reference.setReferenceStatus(StoredObjectReferenceStatus.REFERENCED);
            references.add(reference);
        }
        return references;
    }

    private static void validateCreate(CreateSubmissionCommand command) {
        if (command == null
                || StringUtils.isBlank(command.getTitle())
                || StringUtils.isBlank(command.getContent())
                || StringUtils.isBlank(command.getSourceClientId())) {
            throw invalidParameter("提交内容参数无效");
        }
        if (command.getImageObjectIds() == null) {
            throw invalidParameter("提交内容图片不能为空");
        }
        for (StoredObjectId imageObjectId : command.getImageObjectIds()) {
            if (imageObjectId == null) {
                throw invalidParameter("提交内容图片无效");
            }
        }
    }

    private static BizException invalidParameter(String message) {
        return new BizException("SUBMISSION-00001", "submission.exception.invalid-parameter", message);
    }
}
