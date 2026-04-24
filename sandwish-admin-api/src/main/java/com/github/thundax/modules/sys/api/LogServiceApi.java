package com.github.thundax.modules.sys.api;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.modules.sys.api.query.LogQueryParam;
import com.github.thundax.modules.sys.api.vo.LogVo;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author thundax
 */
@Api(tags = "02-06.系统-日志")
@RequestMapping(value = "/api/sys/log")
public interface LogServiceApi {

    /**
     * 获取列表
     *
     * @param queryParam 查询条件
     * @return 列表
     * @throws ApiException API异常
     */
    @ApiOperation(value = "获取列表", notes = "super")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.HEADER_TOKEN, value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "page", method = RequestMethod.POST)
    PageVo<LogVo> page(@RequestBody @ApiParam("查询条件") LogQueryParam queryParam) throws ApiException;

}
