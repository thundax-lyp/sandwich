package com.github.thundax.modules.assist.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.modules.assist.api.vo.AsyncTaskVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author thundax
 */
@Api(tags = "03-01.辅助-异步任务")
@RequestMapping(value = "/api/assist/async-task")
public interface AsyncTaskServiceApi {

    /**
     * 获取任务
     *
     * @param vo 任务
     * @return 任务
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取对象", notes = "user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "get", method = RequestMethod.POST)
    AsyncTaskVo get(AsyncTaskVo vo) throws ApiException;

}
