package com.github.thundax.modules.audit.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.audit.assembler.AuditInterfaceAssembler;
import com.github.thundax.modules.audit.controller.request.AuditLogPageRequest;
import com.github.thundax.modules.audit.controller.request.AuditMetaRequest;
import com.github.thundax.modules.audit.controller.request.AuditObjectHistoryRequest;
import com.github.thundax.modules.audit.controller.response.AuditLogResponse;
import com.github.thundax.modules.audit.controller.response.AuditMetaResponse;
import com.github.thundax.modules.audit.service.AuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "审计")
@RequestMapping(value = "/api/audit")
@WrappedApiController
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @ApiOperation(value = "获取审计元数据", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "meta", method = RequestMethod.POST)
    public AuditMetaResponse meta(@Valid @RequestBody AuditMetaRequest request) {
        return AuditInterfaceAssembler.toMetaResponse(
                auditService.getMeta(AuditInterfaceAssembler.toMetaQuery(request)));
    }

    @ApiOperation(value = "获取对象审计历史", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "history", method = RequestMethod.POST)
    public List<AuditLogResponse> history(@Valid @RequestBody AuditObjectHistoryRequest request) {
        return auditService.list(AuditInterfaceAssembler.toMetaQuery(request)).stream()
                .map(AuditInterfaceAssembler::toLogResponse)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "审计日志分页", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "page", method = RequestMethod.POST)
    public PageResponse<AuditLogResponse> page(@Valid @RequestBody AuditLogPageRequest request) {
        PageQuery pageQuery = readPage(request);
        return PageResponseHelper.fromEntityPage(
                auditService.page(AuditInterfaceAssembler.toLogQuery(request), pageQuery),
                AuditInterfaceAssembler::toLogResponse);
    }

    private PageQuery readPage(AuditLogPageRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();
        if (pageNo == null || pageNo < PageRules.firstPageIndex()) {
            pageNo = PageRules.firstPageIndex();
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = PageRules.defaultPageSize();
        }
        return new PageQuery(pageNo, pageSize);
    }
}
