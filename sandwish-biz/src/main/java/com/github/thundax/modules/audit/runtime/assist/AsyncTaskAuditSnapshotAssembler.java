package com.github.thundax.modules.audit.runtime.assist;

import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.runtime.AuditSnapshotAssembler;
import com.github.thundax.modules.audit.runtime.AuditSnapshots;
import org.springframework.stereotype.Component;

@Component
public class AsyncTaskAuditSnapshotAssembler implements AuditSnapshotAssembler {

    @Override
    public String objectType() {
        return "AsyncTask";
    }

    @Override
    public AuditSnapshot assemble(Object object) {
        AsyncTask task = (AsyncTask) object;
        if (task == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                task.getId(),
                task.getTitle(),
                AuditSnapshots.field("title", "标题", task.getTitle()),
                AuditSnapshots.field("status", "状态", task.getStatus()),
                AuditSnapshots.field("message", "消息", task.getMessage()));
    }
}
