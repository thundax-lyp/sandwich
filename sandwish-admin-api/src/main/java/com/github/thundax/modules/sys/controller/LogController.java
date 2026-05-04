package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.Page;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.sys.assembler.LogInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.LogPageRequest;
import com.github.thundax.modules.sys.controller.response.LogResponse;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.LogService;
import com.github.thundax.modules.sys.service.OfficeService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.LogQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "02-06.系统-日志")
@RequestMapping(value = "/api/sys/log")
@RestController
public class LogController {

    private final LogService logService;
    private final UserService userService;
    private final OfficeService officeService;

    @Autowired
    public LogController(LogService logService, UserService userService, OfficeService officeService) {
        this.logService = logService;
        this.userService = userService;
        this.officeService = officeService;
    }

    @ApiOperation(value = "获取列表", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "page", method = RequestMethod.POST)
    public PageResponse<LogResponse> page(@Valid @RequestBody LogPageRequest request) throws ApiException {
        LogQuery query = LogInterfaceAssembler.toQuery(request);

        return PageResponseHelper.fromEntityPage(logService.page(query, readLogPage(request)), this::toResponse);
    }

    private LogResponse toResponse(Log log) {
        User user = userService.getById(EntityIdCodec.toDomain(log.getUserId()));
        Office office = user == null ? null : officeService.getById(EntityIdCodec.toDomain(user.getOfficeId()));
        return LogInterfaceAssembler.toResponse(log, user, office, officeService::getById);
    }

    private Page<Log> readLogPage(LogPageRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();

        if (pageNo == null || pageNo < PageRules.firstPageIndex()) {
            pageNo = PageRules.firstPageIndex();
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = PageRules.defaultPageSize();
        }

        Page<Log> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }
}
