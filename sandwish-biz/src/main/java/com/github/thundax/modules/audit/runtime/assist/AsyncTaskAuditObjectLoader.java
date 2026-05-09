package com.github.thundax.modules.audit.runtime.assist;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import com.github.thundax.modules.assist.service.query.AsyncTaskQuery;
import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import org.springframework.stereotype.Component;

@Component
public class AsyncTaskAuditObjectLoader implements AuditObjectLoader {

    private final AsyncTaskService asyncTaskService;

    public AsyncTaskAuditObjectLoader(AsyncTaskService asyncTaskService) {
        this.asyncTaskService = asyncTaskService;
    }

    @Override
    public String objectType() {
        return "AsyncTask";
    }

    @Override
    public Object load(String objectId) {
        AsyncTaskQuery query = new AsyncTaskQuery();
        query.setId(EntityIdCodec.toDomain(Long.valueOf(objectId)));
        return asyncTaskService.get(query);
    }
}
