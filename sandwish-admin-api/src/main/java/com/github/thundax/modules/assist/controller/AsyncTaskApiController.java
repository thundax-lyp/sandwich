package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.api.AsyncTaskServiceApi;
import com.github.thundax.modules.assist.assembler.AsyncTaskInterfaceAssembler;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.request.AsyncTaskIdRequest;
import com.github.thundax.modules.assist.response.AsyncTaskResponse;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** @author thundax */
@RestController
public class AsyncTaskApiController extends BaseApiController implements AsyncTaskServiceApi {

    private final AsyncTaskService asyncTaskService;
    private final AsyncTaskInterfaceAssembler asyncTaskInterfaceAssembler;

    @Autowired
    public AsyncTaskApiController(
            AsyncTaskService asyncTaskService,
            Validator validator,
            AsyncTaskInterfaceAssembler asyncTaskInterfaceAssembler) {
        super(validator);

        this.asyncTaskService = asyncTaskService;
        this.asyncTaskInterfaceAssembler = asyncTaskInterfaceAssembler;
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('user')")
    public AsyncTaskResponse get(@RequestBody AsyncTaskIdRequest request) throws ApiException {
        validate(request);

        AsyncTask bean = asyncTaskService.get(request.getId());
        if (bean == null) {
            return new AsyncTaskResponse();
        }

        if (bean.isPrivate() && !bean.isBelongTo(currentUser())) {
            throw new PermissionDeniedException();
        }

        return asyncTaskInterfaceAssembler.toResponse(bean);
    }
}
