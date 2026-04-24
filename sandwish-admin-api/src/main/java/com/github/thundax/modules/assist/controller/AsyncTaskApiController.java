package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.api.AsyncTaskServiceApi;
import com.github.thundax.modules.assist.api.vo.AsyncTaskVo;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.service.AsyncTaskService;
import com.github.thundax.modules.auth.security.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

/**
 * @author thundax
 */
@RestController
public class AsyncTaskApiController extends BaseApiController implements AsyncTaskServiceApi {

    private final AsyncTaskService asyncTaskService;

    @Autowired
    public AsyncTaskApiController(AsyncTaskService asyncTaskService, Validator validator) {
        super(validator);

        this.asyncTaskService = asyncTaskService;
    }


    @Override
    @RequiresPermissions("user")
    public AsyncTaskVo get(@RequestBody AsyncTaskVo vo) throws ApiException {
        AsyncTask bean = asyncTaskService.get(vo.getId());
        if (bean == null) {
            return new AsyncTaskVo();
        }

        if (bean.isPrivate() && !bean.isBelongTo(currentUser())) {
            throw new PermissionDeniedException();
        }

        return entityToVo(asyncTaskService.get(vo.getId()));
    }


    @NonNull
    private AsyncTaskVo entityToVo(AsyncTask entity) {
        if (entity == null) {
            return new AsyncTaskVo();
        }

        AsyncTaskVo vo = baseEntityToVo(new AsyncTaskVo(), entity);

        vo.setStatus(entity.getStatus());
        vo.setMessage(entity.getMessage());
        vo.setData(entity.getData());

        return vo;
    }

}
