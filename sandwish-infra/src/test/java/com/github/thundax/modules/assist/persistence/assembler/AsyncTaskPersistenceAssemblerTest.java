package com.github.thundax.modules.assist.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.enums.AsyncTaskStatus;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskId;
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
        entity.setId(AsyncTaskId.of(6001L));
        entity.setStatus(AsyncTaskStatus.SUCCESS);

        AsyncTaskDO dataObject = AsyncTaskPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(6001L), dataObject.getId());
        assertEquals("SUCCESS", dataObject.getStatus());
    }

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        AsyncTask entity = new AsyncTask();
        entity.setPriority(-1);
        AsyncTaskDO dataObject = new AsyncTaskDO();
        dataObject.setStatus("IDLE");
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                AsyncTaskPersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, AsyncTaskPersistenceAssembler.toEntity(dataObject).getPriority());
    }
}
