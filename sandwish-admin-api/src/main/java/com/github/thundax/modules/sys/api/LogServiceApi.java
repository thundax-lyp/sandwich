package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.modules.sys.request.LogPageRequest;
import com.github.thundax.modules.sys.response.LogResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "02-06.系统-日志")
@RequestMapping(value = "/api/sys/log")
public interface LogServiceApi {

    /**
     * 获取列表
     *
     * @param request 日志分页查询请求
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "page", method = RequestMethod.POST)
    PageVo<LogResponse> page(@RequestBody @ApiParam("日志分页查询请求") LogPageRequest request) throws ApiException;
}
