package com.github.thundax.modules.audit.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.request.PageRequest;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.audit.assembler.AuditInterfaceAssembler;
import com.github.thundax.modules.audit.controller.request.AuditLogDetailRequest;
import com.github.thundax.modules.audit.controller.request.AuditLogPageRequest;
import com.github.thundax.modules.audit.controller.request.AuditMetaRequest;
import com.github.thundax.modules.audit.controller.request.AuditObjectFieldRequest;
import com.github.thundax.modules.audit.controller.request.AuditObjectHistoryRequest;
import com.github.thundax.modules.audit.controller.request.AuditObjectPageRequest;
import com.github.thundax.modules.audit.controller.response.AuditLogDetailResponse;
import com.github.thundax.modules.audit.controller.response.AuditLogResponse;
import com.github.thundax.modules.audit.controller.response.AuditMetaResponse;
import com.github.thundax.modules.audit.controller.response.AuditObjectFieldResponse;
import com.github.thundax.modules.audit.controller.response.AuditObjectOverviewResponse;
import com.github.thundax.modules.audit.controller.response.AuditOptionsResponse;
import com.github.thundax.modules.audit.service.AuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "审计")
@RequestMapping(value = "/api/audit/log")
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
    @PostMapping(value = "meta")
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
    @PostMapping(value = "history")
    public List<AuditLogResponse> history(@Valid @RequestBody AuditObjectHistoryRequest request) {
        return auditService.list(AuditInterfaceAssembler.toMetaQuery(request)).stream()
                .map(AuditInterfaceAssembler::toLogResponse)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "获取审计日志详情", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "detail")
    public AuditLogDetailResponse detail(@Valid @RequestBody AuditLogDetailRequest request) {
        return AuditInterfaceAssembler.toLogDetailResponse(
                auditService.getLog(AuditInterfaceAssembler.toLogQuery(request)));
    }

    @ApiOperation(value = "获取对象审计概览", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "object/overview")
    public AuditObjectOverviewResponse objectOverview(@Valid @RequestBody AuditMetaRequest request) {
        return AuditInterfaceAssembler.toOverviewResponse(
                auditService.getMeta(AuditInterfaceAssembler.toMetaQuery(request)),
                auditService.page(
                        AuditInterfaceAssembler.toObjectLogQuery(request),
                        new PageQuery(PageRules.firstPageIndex(), 5)));
    }

    @ApiOperation(value = "获取对象审计分页", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "object/page")
    public PageResponse<AuditLogResponse> objectPage(@Valid @RequestBody AuditObjectPageRequest request) {
        return PageResponseHelper.fromPageResult(
                auditService.page(AuditInterfaceAssembler.toLogQuery(request), readPage(request)),
                AuditInterfaceAssembler::toLogResponse);
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
    @PostMapping(value = "page")
    public PageResponse<AuditLogResponse> page(@Valid @RequestBody AuditLogPageRequest request) {
        PageQuery pageQuery = readPage(request);
        return PageResponseHelper.fromPageResult(
                auditService.page(AuditInterfaceAssembler.toLogQuery(request), pageQuery),
                AuditInterfaceAssembler::toLogResponse);
    }

    @ApiOperation(value = "获取审计选项", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "options")
    public AuditOptionsResponse options() {
        return AuditInterfaceAssembler.toOptionsResponse();
    }

    @ApiOperation(value = "获取审计对象字段", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "fields")
    public List<AuditObjectFieldResponse> fields(@Valid @RequestBody AuditObjectFieldRequest request) {
        return AuditInterfaceAssembler.toFieldResponses(request.getObjectType());
    }

    private PageQuery readPage(PageRequest request) {
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
