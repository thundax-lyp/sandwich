package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.query.PrincipalIdentityQuery;
import com.github.thundax.modules.sys.assembler.LogInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.LogPageRequest;
import com.github.thundax.modules.sys.controller.response.LogResponse;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.LogService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.LogQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "系统/日志")
@RequestMapping(value = "/api/sys/log")
@WrappedApiController
public class LogController {

    private final LogService logService;
    private final UserService userService;
    private final PrincipalIdentityService principalIdentityService;
    private final DepartmentService departmentService;

    @Autowired
    public LogController(
            LogService logService,
            UserService userService,
            PrincipalIdentityService principalIdentityService,
            DepartmentService departmentService) {
        this.logService = logService;
        this.userService = userService;
        this.principalIdentityService = principalIdentityService;
        this.departmentService = departmentService;
    }

    @ApiOperation(value = "获取列表", notes = "super")
    @HasPermission("super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "page")
    public PageResponse<LogResponse> page(@Valid @RequestBody LogPageRequest request) {
        LogQuery query = LogInterfaceAssembler.toQuery(request);

        return PageResponseHelper.fromPageResult(logService.page(query, readLogPage(request)), this::toResponse);
    }

    private LogResponse toResponse(Log log) {
        User user = userService.get(UserIdCodec.toDomain(Long.valueOf(log.getUserId())));
        Department department = user == null ? null : departmentService.get(user.getDepartmentId());
        return LogInterfaceAssembler.toResponse(
                log, user, getAccountLoginName(user), department, departmentService::get);
    }

    private String getAccountLoginName(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        PrincipalIdentity identity = principalIdentityService.get(identityQuery(
                PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(user.getId())),
                PrincipalIdentityType.USER_ACCOUNT));
        return identity == null ? null : identity.getIdentityValue();
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }

    private PageQuery readLogPage(LogPageRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();

        if (pageNo == null || pageNo < PageRules.firstPageIndex()) {
            pageNo = PageRules.firstPageIndex();
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = PageRules.defaultPageSize();
        }

        PageQuery page = new PageQuery();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }
}
