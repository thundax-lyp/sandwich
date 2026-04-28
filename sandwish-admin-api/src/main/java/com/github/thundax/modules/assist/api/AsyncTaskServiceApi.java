package com.github.thundax.modules.assist.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.assist.request.AsyncTaskIdRequest;
import com.github.thundax.modules.assist.response.AsyncTaskResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "03-01.辅助-异步任务")
@RequestMapping(value = "/api/assist/async-task")
public interface AsyncTaskServiceApi {

    /**
     * 获取任务
     *
     * @param request 任务标识请求
     * @return 任务
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取对象", notes = "user")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "get", method = RequestMethod.POST)
    AsyncTaskResponse get(@RequestBody @ApiParam("异步任务标识请求") AsyncTaskIdRequest request) throws ApiException;
}
