package com.github.thundax.modules.assist.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.i18n.I18nMessages;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.assist.controller.request.AsyncTaskIdRequest;
import com.github.thundax.modules.assist.controller.response.AsyncTaskResponse;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.enums.AsyncTaskStatus;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import java.util.Locale;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticMessageSource;

public class AsyncTaskControllerContractTest {

    @BeforeClass
    public static void setUpMessages() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("common.exception.permission-denied", Locale.getDefault(), "permission denied");
        new I18nMessages(messageSource);
    }

    @After
    public void tearDown() {
        UserAccessHolder.clear();
        SpringContextHolder.clearHolder();
    }

    @Test
    public void shouldReturnEmptyResponseWhenTaskMissing() throws Exception {
        AsyncTaskService asyncTaskService = mock(AsyncTaskService.class);
        AsyncTaskController controller = new AsyncTaskController(asyncTaskService);

        AsyncTaskResponse response = controller.get(idRequest("missing"));

        assertNull(response.getId());
    }

    @Test
    public void shouldReturnPublicTaskResponse() throws Exception {
        AsyncTaskService asyncTaskService = mock(AsyncTaskService.class);
        AsyncTaskController controller = new AsyncTaskController(asyncTaskService);
        AsyncTask task = task("task-1");
        task.setStatus(AsyncTaskStatus.SUCCESS);
        task.setMessage("done");
        task.setData("{\"ok\":true}");
        when(asyncTaskService.getById(EntityId.of("task-1"))).thenReturn(task);

        AsyncTaskResponse response = controller.get(idRequest("task-1"));

        assertEquals("task-1", response.getId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("done", response.getMessage());
        assertEquals("{\"ok\":true}", response.getData());
    }

    @Test(expected = PermissionDeniedException.class)
    public void shouldRejectPrivateTaskWhenCurrentUserDoesNotOwnIt() throws Exception {
        AsyncTaskService asyncTaskService = mock(AsyncTaskService.class);
        AsyncTaskController controller = new AsyncTaskController(asyncTaskService);
        AsyncTask task = task("task-1");
        task.setPrivate(true);
        task.setCreateUserId("owner-1");
        when(asyncTaskService.getById(EntityId.of("task-1"))).thenReturn(task);
        UserAccessHolder.currentUserId("other-user", "token-1");
        mockCurrentUser("other-user");

        controller.get(idRequest("task-1"));
    }

    private AsyncTaskIdRequest idRequest(String id) {
        AsyncTaskIdRequest request = new AsyncTaskIdRequest();
        request.setId(id);
        return request;
    }

    private AsyncTask task(String id) {
        AsyncTask task = new AsyncTask();
        task.setId(EntityId.of(id));
        task.setTitle(id);
        return task;
    }

    private void mockCurrentUser(String userId) {
        User currentUser = new User();
        currentUser.setId(EntityId.of(userId));
        UserService userService = mock(UserService.class);
        when(userService.getById(EntityId.of(userId))).thenReturn(currentUser);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(UserService.class)).thenReturn(userService);
        SpringContextHolder.setApplicationContext(applicationContext);
    }
}
