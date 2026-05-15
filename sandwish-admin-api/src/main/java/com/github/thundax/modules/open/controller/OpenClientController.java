package com.github.thundax.modules.open.controller;

import com.github.thundax.common.exception.AdminResponseExceptions;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.token.AccessTokenNames;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.assembler.PageInterfaceAssembler;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.open.assembler.OpenClientInterfaceAssembler;
import com.github.thundax.modules.open.controller.request.OpenClientIdRequest;
import com.github.thundax.modules.open.controller.request.OpenClientPageRequest;
import com.github.thundax.modules.open.controller.request.OpenClientSaveRequest;
import com.github.thundax.modules.open.controller.request.OpenClientSecretResetRequest;
import com.github.thundax.modules.open.controller.request.OpenClientStatusRequest;
import com.github.thundax.modules.open.controller.response.OpenClientResponse;
import com.github.thundax.modules.open.controller.response.OpenClientSecretResponse;
import com.github.thundax.modules.open.entity.valueobject.OpenClientIdCodec;
import com.github.thundax.modules.open.service.OpenClientService;
import com.github.thundax.modules.open.service.command.ResetOpenClientSecretCommand;
import com.github.thundax.modules.open.service.dto.OpenClientDTO;
import com.github.thundax.modules.open.service.query.OpenClientQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "开放客户端")
@RequestMapping(value = "/api/open/client")
@WrappedApiController
public class OpenClientController {

    private final OpenClientService openClientService;

    public OpenClientController(OpenClientService openClientService) {
        this.openClientService = openClientService;
    }

    @ApiOperation(value = "创建开放客户端", notes = "open:client:edit")
    @HasPermission("open:client:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "create")
    public OpenClientSecretResponse create(@Valid @RequestBody OpenClientSaveRequest request) {
        return OpenClientInterfaceAssembler.toSecretResponse(
                openClientService.create(OpenClientInterfaceAssembler.toCreateCommand(request)));
    }

    @ApiOperation(value = "更新开放客户端", notes = "open:client:edit")
    @HasPermission("open:client:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "update")
    public OpenClientResponse update(@Valid @RequestBody OpenClientSaveRequest request) {
        return OpenClientInterfaceAssembler.toResponse(
                openClientService.change(OpenClientInterfaceAssembler.toUpdateCommand(request)));
    }

    @ApiOperation(value = "获取分页列表", notes = "open:client:view")
    @HasPermission("open:client:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "page")
    public PageResponse<OpenClientResponse> page(@Valid @RequestBody OpenClientPageRequest request) {
        OpenClientQuery query = OpenClientInterfaceAssembler.toQuery(request);
        return PageResponseHelper.fromPageResult(
                openClientService.page(query, PageInterfaceAssembler.toPageQuery(request)),
                OpenClientInterfaceAssembler::toResponse);
    }

    @ApiOperation(value = "获取对象", notes = "open:client:view")
    @HasPermission("open:client:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "get")
    public OpenClientResponse get(@Valid @RequestBody OpenClientIdRequest request) {
        OpenClientDTO dto = openClientService.get(OpenClientIdCodec.toDomain(request.getId()));
        if (dto == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        return OpenClientInterfaceAssembler.toResponse(dto);
    }

    @ApiOperation(value = "调整状态", notes = "open:client:edit")
    @HasPermission("open:client:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "change-status")
    public Boolean changeStatus(@Valid @RequestBody OpenClientStatusRequest request) {
        openClientService.changeStatus(OpenClientInterfaceAssembler.toChangeStatusCommand(request));
        return true;
    }

    @ApiOperation(value = "重置API SECRET", notes = "open:client:edit")
    @HasPermission("open:client:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "secret/reset")
    public OpenClientSecretResponse resetSecret(@Valid @RequestBody OpenClientSecretResetRequest request) {
        return OpenClientInterfaceAssembler.toSecretResponse(openClientService.resetSecret(
                new ResetOpenClientSecretCommand(OpenClientIdCodec.toDomain(request.getId()))));
    }
}
