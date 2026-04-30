package com.github.thundax.modules.assist.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.AsyncTaskStatus;
import com.github.thundax.modules.assist.persistence.dataobject.AsyncTaskDO;
import org.junit.Test;

public class AsyncTaskPersistenceAssemblerTest {

    @Test
    public void shouldReadLegacyLowerCaseStatus() {
        AsyncTaskDO dataObject = new AsyncTaskDO();
        dataObject.setStatus("idle");

        AsyncTask entity = AsyncTaskPersistenceAssembler.toEntity(dataObject);

        assertSame(AsyncTaskStatus.IDLE, entity.getStatus());
    }

    @Test
    public void shouldWriteEnumStatusValue() {
        AsyncTask entity = new AsyncTask();
        entity.setStatus(AsyncTaskStatus.SUCCESS);

        AsyncTaskDO dataObject = AsyncTaskPersistenceAssembler.toDataObject(entity);

        assertEquals("SUCCESS", dataObject.getStatus());
    }
}
