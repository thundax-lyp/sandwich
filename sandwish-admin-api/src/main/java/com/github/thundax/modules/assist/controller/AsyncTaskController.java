package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.request.RequestListHelper;
import com.github.thundax.modules.assist.assembler.AsyncTaskInterfaceAssembler;
import com.github.thundax.modules.assist.controller.request.AsyncTaskIdRequest;
import com.github.thundax.modules.assist.controller.request.AsyncTaskSortRequest;
import com.github.thundax.modules.assist.controller.response.AsyncTaskResponse;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskIdCodec;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import com.github.thundax.modules.assist.service.command.AsyncTaskSortCommand;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "辅助/异步任务")
@RequestMapping(value = "/api/assist/async-task")
@WrappedApiController
public class AsyncTaskController {

    private final AsyncTaskService asyncTaskService;

    @Autowired
    public AsyncTaskController(AsyncTaskService asyncTaskService) {

        this.asyncTaskService = asyncTaskService;
    }

    @ApiOperation(value = "获取对象", notes = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("user")
    @PostMapping(value = "get")
    public AsyncTaskResponse get(@Valid @RequestBody AsyncTaskIdRequest request) throws ApiException {
        AsyncTask bean = asyncTaskService.get(AsyncTaskIdCodec.toDomain(request.getId()));
        if (bean == null) {
            return AsyncTaskInterfaceAssembler.toResponse(null);
        }

        if (bean.isPrivate() && !bean.isBelongTo(UserAccessHolder.currentUser())) {
            throw new PermissionDeniedException();
        }

        return AsyncTaskInterfaceAssembler.toResponse(bean);
    }

    @ApiOperation(value = "排序", notes = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("user")
    @PostMapping(value = "sort")
    public Boolean sort(@Valid @RequestBody AsyncTaskSortRequest request) throws ApiException {
        asyncTaskService.sort(new AsyncTaskSortCommand(
                RequestListHelper.map(request == null ? null : request.getOrderedIds(), AsyncTaskIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }
}
