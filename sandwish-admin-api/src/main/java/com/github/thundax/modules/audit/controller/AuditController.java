package com.github.thundax.modules.audit.controller;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.token.AccessTokenNames;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.assembler.PageInterfaceAssembler;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.audit.assembler.AuditInterfaceAssembler;
import com.github.thundax.modules.audit.controller.request.AuditLogDetailRequest;
import com.github.thundax.modules.audit.controller.request.AuditLogPageRequest;
import com.github.thundax.modules.audit.controller.request.AuditMetaRequest;
import com.github.thundax.modules.audit.controller.request.AuditObjectFieldRequest;
import com.github.thundax.modules.audit.controller.request.AuditObjectPageRequest;
import com.github.thundax.modules.audit.controller.response.AuditLogDetailResponse;
import com.github.thundax.modules.audit.controller.response.AuditLogResponse;
import com.github.thundax.modules.audit.controller.response.AuditMetaResponse;
import com.github.thundax.modules.audit.controller.response.AuditObjectFieldResponse;
import com.github.thundax.modules.audit.controller.response.AuditObjectOverviewResponse;
import com.github.thundax.modules.audit.controller.response.AuditOptionsResponse;
import com.github.thundax.modules.audit.runtime.AuditSnapshotAssemblerRegistry;
import com.github.thundax.modules.audit.service.AuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "审计")
@RequestMapping(value = "/api/audit/log")
@WrappedApiController
public class AuditController {

    private final AuditService auditService;
    private final AuditSnapshotAssemblerRegistry auditSnapshotAssemblerRegistry;

    public AuditController(AuditService auditService, AuditSnapshotAssemblerRegistry auditSnapshotAssemblerRegistry) {
        this.auditService = auditService;
        this.auditSnapshotAssemblerRegistry = auditSnapshotAssemblerRegistry;
    }

    @ApiOperation(value = "获取审计元数据", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
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
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "history")
    public PageResponse<AuditLogResponse> history(@Valid @RequestBody AuditObjectPageRequest request) {
        return PageResponseHelper.fromPageResult(
                auditService.page(
                        AuditInterfaceAssembler.toLogQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                log -> AuditInterfaceAssembler.toLogResponse(log, auditSnapshotAssemblerRegistry));
    }

    @ApiOperation(value = "获取审计日志详情", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "detail")
    public AuditLogDetailResponse detail(@Valid @RequestBody AuditLogDetailRequest request) {
        return AuditInterfaceAssembler.toLogDetailResponse(
                auditService.getLog(AuditInterfaceAssembler.toLogId(request)), auditSnapshotAssemblerRegistry);
    }

    @ApiOperation(value = "获取对象审计概览", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
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
                        new PageQuery(PageRules.firstPageIndex(), 5)),
                auditSnapshotAssemblerRegistry);
    }

    @ApiOperation(value = "获取对象审计分页", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "object/page")
    public PageResponse<AuditLogResponse> objectPage(@Valid @RequestBody AuditObjectPageRequest request) {
        return PageResponseHelper.fromPageResult(
                auditService.page(
                        AuditInterfaceAssembler.toLogQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                log -> AuditInterfaceAssembler.toLogResponse(log, auditSnapshotAssemblerRegistry));
    }

    @ApiOperation(value = "审计日志分页", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "page")
    public PageResponse<AuditLogResponse> page(@Valid @RequestBody AuditLogPageRequest request) {
        PageQuery pageQuery = PageInterfaceAssembler.toPageQuery(request);
        return PageResponseHelper.fromPageResult(
                auditService.page(AuditInterfaceAssembler.toLogQuery(request), pageQuery),
                log -> AuditInterfaceAssembler.toLogResponse(log, auditSnapshotAssemblerRegistry));
    }

    @ApiOperation(value = "获取审计选项", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "options")
    public AuditOptionsResponse options() {
        return AuditInterfaceAssembler.toOptionsResponse(auditSnapshotAssemblerRegistry);
    }

    @ApiOperation(value = "获取审计对象字段", notes = "audit:view")
    @HasPermission("audit:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "fields")
    public List<AuditObjectFieldResponse> fields(@Valid @RequestBody AuditObjectFieldRequest request) {
        return AuditInterfaceAssembler.toFieldResponses(auditSnapshotAssemblerRegistry, request.getObjectType());
    }
}
