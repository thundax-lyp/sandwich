package com.github.thundax.modules.assist.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.service.command.AsyncTaskCommand;
import com.github.thundax.modules.assist.service.query.AsyncTaskQuery;
import org.junit.Test;

public class AsyncTaskServiceImplTest {

    @Test
    public void shouldGetTaskByQueryId() {
        RecordingAsyncTaskDao dao = new RecordingAsyncTaskDao();
        AsyncTask task = task(EntityId.of(1001L));
        dao.getResult = task;
        AsyncTaskServiceImpl service = new AsyncTaskServiceImpl(dao);

        AsyncTask result = service.get(new AsyncTaskQuery(EntityId.of(1001L), null, null));

        assertSame(task, result);
        assertEquals(EntityId.of(1001L), dao.getId);
    }

    @Test
    public void shouldReturnNullWhenQueryHasNoId() {
        RecordingAsyncTaskDao dao = new RecordingAsyncTaskDao();
        AsyncTaskServiceImpl service = new AsyncTaskServiceImpl(dao);

        assertNull(service.get(new AsyncTaskQuery()));
        assertNull(service.get(null));
        assertNull(dao.getId);
    }

    @Test
    public void shouldCreateTaskWithCommandPayload() {
        RecordingAsyncTaskDao dao = new RecordingAsyncTaskDao();
        AsyncTask task = task(null);
        AsyncTaskServiceImpl service = new AsyncTaskServiceImpl(dao);

        EntityId id = service.create(new AsyncTaskCommand(null, task));

        assertEquals(EntityId.of(9001L), id);
        assertSame(task, dao.inserted);
    }

    @Test
    public void shouldChangeTaskWithCommandPayload() {
        RecordingAsyncTaskDao dao = new RecordingAsyncTaskDao();
        AsyncTask task = task(EntityId.of(1001L));
        AsyncTaskServiceImpl service = new AsyncTaskServiceImpl(dao);

        service.change(new AsyncTaskCommand(null, task));

        assertSame(task, dao.updated);
    }

    @Test
    public void shouldRemoveTaskWithCommandId() {
        RecordingAsyncTaskDao dao = new RecordingAsyncTaskDao();
        AsyncTaskServiceImpl service = new AsyncTaskServiceImpl(dao);

        service.remove(new AsyncTaskCommand(EntityId.of(1001L), null));

        assertEquals(EntityId.of(1001L), dao.deletedId);
    }

    private AsyncTask task(EntityId id) {
        AsyncTask task = new AsyncTask();
        task.setId(id);
        return task;
    }

    private static class RecordingAsyncTaskDao implements AsyncTaskDao {

        private EntityId getId;
        private EntityId deletedId;
        private AsyncTask getResult;
        private AsyncTask inserted;
        private AsyncTask updated;

        @Override
        public AsyncTask getById(EntityId id) {
            this.getId = id;
            return getResult;
        }

        @Override
        public EntityId insert(AsyncTask asyncTask) {
            this.inserted = asyncTask;
            return EntityId.of(9001L);
        }

        @Override
        public void update(AsyncTask asyncTask) {
            this.updated = asyncTask;
        }

        @Override
        public void deleteById(EntityId id) {
            this.deletedId = id;
        }
    }
}
