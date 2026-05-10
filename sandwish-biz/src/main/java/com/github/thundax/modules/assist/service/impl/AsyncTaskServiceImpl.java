package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.ErrorCode;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskId;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import com.github.thundax.modules.assist.service.command.AsyncTaskCommand;
import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private static final int PRIORITY_STEP = 10;

    private final AsyncTaskDao asyncTaskDao;

    public AsyncTaskServiceImpl(AsyncTaskDao asyncTaskDao) {
        this.asyncTaskDao = asyncTaskDao;
    }

    @Override
    public AsyncTask get(AsyncTaskId id) {
        if (id == null) {
            return null;
        }
        return asyncTaskDao.getById(id);
    }

    @Override
    @AuditLog(type = "AsyncTask", id = "", action = AuditAction.CREATE, summary = "创建异步任务", recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public AsyncTaskId create(AsyncTaskCommand command) {
        AsyncTask asyncTask = command.getAsyncTask();
        if (asyncTask != null && asyncTask.getId() == null) {
            asyncTask.setPriority(asyncTaskDao.maxPriority() + PRIORITY_STEP);
        }
        return asyncTaskDao.insert(asyncTask);
    }

    @Override
    @AuditLog(type = "AsyncTask", id = "#command.asyncTask.id.value()", action = AuditAction.UPDATE, summary = "更新异步任务")
    @Transactional(rollbackFor = Exception.class)
    public void change(AsyncTaskCommand command) {
        asyncTaskDao.update(command.getAsyncTask());
    }

    @Override
    @AuditLog(
            type = "AsyncTask",
            id = "#id.value()",
            action = AuditAction.DELETE,
            summary = "删除异步任务",
            recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public void remove(AsyncTaskId id) {
        asyncTaskDao.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(List<AsyncTaskId> orderedIds, SortDirection sortDirection) throws ApiException {
        SortDirection effectiveDirection = sortDirection == null ? SortDirection.ASC : sortDirection;
        List<AsyncTaskId> orderedIdList = normalizeOrderedIds(orderedIds);
        if (orderedIdList.isEmpty()) {
            throw new ApiException(ErrorCode.SORT_EMPTY_INPUT.getCode(), ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        List<AsyncTask> currentTasks = asyncTaskDao.list(effectiveDirection);
        if (currentTasks == null || currentTasks.isEmpty()) {
            throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
        }

        if (currentTasks.size() != orderedIdList.size()) {
            throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
        }

        Map<Long, Integer> indexById = new HashMap<>(currentTasks.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentTasks.size());
        List<AsyncTaskId> currentOrderedIds = new ArrayList<>(currentTasks.size());

        for (int i = 0; i < currentTasks.size(); i++) {
            AsyncTask asyncTask = currentTasks.get(i);
            if (asyncTask == null || asyncTask.getId() == null) {
                throw new ApiException(ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessage());
            }
            long asyncTaskId = asyncTask.getId().value();
            indexById.put(asyncTaskId, i);
            priorityById.put(asyncTaskId, asyncTask.getPriority());
            currentOrderedIds.add(asyncTask.getId());
        }

        for (AsyncTaskId orderedId : orderedIdList) {
            if (orderedId == null || !indexById.containsKey(orderedId.value())) {
                throw new ApiException(ErrorCode.SORT_MISSING_ID.getCode(), ErrorCode.SORT_MISSING_ID.getMessage());
            }
        }

        try {
            int temporaryPriority = asyncTaskDao.maxPriority() + PRIORITY_STEP;
            for (int i = 0; i < currentOrderedIds.size(); i++) {
                AsyncTaskId targetId = orderedIdList.get(i);
                AsyncTaskId currentId = currentOrderedIds.get(i);
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

    private List<AsyncTaskId> normalizeOrderedIds(List<AsyncTaskId> orderedIds) throws ApiException {
        if (orderedIds == null) {
            return new ArrayList<>();
        }
        Set<Long> uniqueIdValues = new HashSet<>(orderedIds.size());
        List<AsyncTaskId> normalized = new ArrayList<>(orderedIds.size());
        for (AsyncTaskId orderedId : orderedIds) {
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

    private void updatePriorityOrThrow(AsyncTaskId id, int priority, String message) throws ApiException {
        int updated = asyncTaskDao.updatePriority(id, priority);
        if (updated != 1) {
            throw new ApiException(ErrorCode.SORT_DB_FAILURE.getCode(), message);
        }
    }
}
