package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.assembler.AsyncTaskInterfaceAssembler;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.request.AsyncTaskIdRequest;
import com.github.thundax.modules.assist.response.AsyncTaskResponse;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "03-01.辅助-异步任务")
@RequestMapping(value = "/api/assist/async-task")
@RestController
public class AsyncTaskApiController extends BaseApiController {

    private final AsyncTaskService asyncTaskService;

    @Autowired
    public AsyncTaskApiController(AsyncTaskService asyncTaskService) {

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
    @RequestMapping(value = "get", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('user')")
    public AsyncTaskResponse get(@Valid @RequestBody AsyncTaskIdRequest request) throws ApiException {
        AsyncTask bean = asyncTaskService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            return new AsyncTaskResponse();
        }

        if (bean.isPrivate() && !bean.isBelongTo(UserAccessHolder.currentUser())) {
            throw new PermissionDeniedException();
        }

        return AsyncTaskInterfaceAssembler.toResponse(bean);
    }
}
