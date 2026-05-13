package com.github.thundax.modules.sys.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.sys.controller.request.LogPageRequest;
import com.github.thundax.modules.sys.controller.response.LogResponse;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.enums.LogType;
import com.github.thundax.modules.sys.entity.valueobject.LogIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.LogService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.LogQuery;
import java.util.Collections;
import java.util.Date;
import org.junit.Test;

public class LogControllerContractTest {

    @Test
    public void shouldPageLogsWithoutUserId() {
        LogService logService = mock(LogService.class);
        UserService userService = mock(UserService.class);
        Log log = log(null);
        when(logService.page(any(LogQuery.class), any(PageQuery.class)))
                .thenReturn(PageResult.of(1, 10, 1L, Collections.singletonList(log)));
        LogController controller = new LogController(
                logService, userService, mock(PrincipalIdentityService.class), mock(DepartmentService.class));

        PageResponse<LogResponse> response = controller.page(new LogPageRequest());

        assertEquals(1, response.getCount());
        assertEquals("访问首页", response.getRecords().get(0).getTitle());
        verify(userService, never()).get(any(UserId.class));
    }

    private Log log(String userId) {
        Log log = new Log();
        log.setId(LogIdCodec.toDomain(1001L));
        log.setUserId(userId);
        log.setType(LogType.ACCESS);
        log.setTitle("访问首页");
        log.setLogDate(new Date());
        return log;
    }
}
