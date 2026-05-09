package com.github.thundax.modules.assist.service.impl;

import static org.junit.Assert.*;

import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskId;
import com.github.thundax.modules.assist.service.command.AsyncTaskCommand;
import org.junit.Test;

public class AsyncTaskServiceImplTest {

    @Test
    public void shouldGetTaskByQueryId() {
        RecordingAsyncTaskDao dao = new RecordingAsyncTaskDao();
        AsyncTask task = task(AsyncTaskId.of(1001L));
        dao.getResult = task;
        AsyncTaskServiceImpl service = new AsyncTaskServiceImpl(dao);

        AsyncTask result = service.get(AsyncTaskId.of(1001L));

        assertSame(task, result);
        assertEquals(AsyncTaskId.of(1001L), dao.getId);
    }

    @Test
    public void shouldReturnNullWhenQueryHasNoId() {
        RecordingAsyncTaskDao dao = new RecordingAsyncTaskDao();
        AsyncTaskServiceImpl service = new AsyncTaskServiceImpl(dao);

        assertNull(service.get(null));
        assertNull(dao.getId);
    }

    @Test
    public void shouldCreateTaskWithCommandPayload() {
        RecordingAsyncTaskDao dao = new RecordingAsyncTaskDao();
        AsyncTask task = task(null);
        AsyncTaskServiceImpl service = new AsyncTaskServiceImpl(dao);

        AsyncTaskId id = service.create(new AsyncTaskCommand(null, task));

        assertEquals(AsyncTaskId.of(9001L), id);
        assertSame(task, dao.inserted);
    }

    @Test
    public void shouldChangeTaskWithCommandPayload() {
        RecordingAsyncTaskDao dao = new RecordingAsyncTaskDao();
        AsyncTask task = task(AsyncTaskId.of(1001L));
        AsyncTaskServiceImpl service = new AsyncTaskServiceImpl(dao);

        service.change(new AsyncTaskCommand(null, task));

        assertSame(task, dao.updated);
    }

    @Test
    public void shouldRemoveTaskWithCommandId() {
        RecordingAsyncTaskDao dao = new RecordingAsyncTaskDao();
        AsyncTaskServiceImpl service = new AsyncTaskServiceImpl(dao);

        service.remove(AsyncTaskId.of(1001L));

        assertEquals(AsyncTaskId.of(1001L), dao.deletedId);
    }

    private AsyncTask task(AsyncTaskId id) {
        AsyncTask task = new AsyncTask();
        task.setId(id);
        return task;
    }

    private static class RecordingAsyncTaskDao implements AsyncTaskDao {

        private AsyncTaskId getId;
        private AsyncTaskId deletedId;
        private AsyncTask getResult;
        private AsyncTask inserted;
        private AsyncTask updated;

        @Override
        public AsyncTask getById(AsyncTaskId id) {
            this.getId = id;
            return getResult;
        }

        @Override
        public AsyncTaskId insert(AsyncTask asyncTask) {
            this.inserted = asyncTask;
            return AsyncTaskId.of(9001L);
        }

        @Override
        public void update(AsyncTask asyncTask) {
            this.updated = asyncTask;
        }

        @Override
        public void deleteById(AsyncTaskId id) {
            this.deletedId = id;
        }
    }
}
