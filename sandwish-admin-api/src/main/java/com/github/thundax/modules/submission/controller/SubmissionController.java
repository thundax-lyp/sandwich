package com.github.thundax.modules.submission.controller;

import com.github.thundax.common.exception.AdminResponseExceptions;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.token.AccessTokenNames;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.assembler.PageInterfaceAssembler;
import com.github.thundax.common.web.request.RequestListHelper;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.submission.assembler.SubmissionInterfaceAssembler;
import com.github.thundax.modules.submission.controller.request.SubmissionIdRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionPageRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionSortRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionStatusRequest;
import com.github.thundax.modules.submission.controller.response.SubmissionResponse;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionIdCodec;
import com.github.thundax.modules.submission.service.SubmissionService;
import com.github.thundax.modules.submission.service.command.SubmissionSortCommand;
import com.github.thundax.modules.submission.service.query.SubmissionQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "提交内容")
@RequestMapping(value = "/api/submission/submission")
@WrappedApiController
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @ApiOperation(value = "获取分页列表", notes = "submission:submission:view")
    @HasPermission("submission:submission:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "page")
    public PageResponse<SubmissionResponse> page(@Valid @RequestBody SubmissionPageRequest request) {
        SubmissionQuery query = SubmissionInterfaceAssembler.toQuery(request);
        return PageResponseHelper.fromPageResult(
                submissionService.page(query, PageInterfaceAssembler.toPageQuery(request)),
                SubmissionInterfaceAssembler::toResponse);
    }

    @ApiOperation(value = "获取对象", notes = "submission:submission:view")
    @HasPermission("submission:submission:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "get")
    public SubmissionResponse get(@Valid @RequestBody SubmissionIdRequest request) {
        Submission submission = submissionService.get(SubmissionInterfaceAssembler.toId(request));
        if (submission == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        return SubmissionInterfaceAssembler.toResponse(submission);
    }

    @ApiOperation(value = "调整状态", notes = "submission:submission:edit")
    @HasPermission("submission:submission:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "change-status")
    public Boolean changeStatus(@Valid @RequestBody SubmissionStatusRequest request) {
        submissionService.changeStatus(SubmissionInterfaceAssembler.toChangeStatusCommand(request));
        return true;
    }

    @ApiOperation(value = "排序", notes = "submission:submission:edit")
    @HasPermission("submission:submission:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "sort")
    public Boolean sort(@Valid @RequestBody SubmissionSortRequest request) {
        submissionService.sort(new SubmissionSortCommand(
                RequestListHelper.map(
                        readOrderedIds(request == null ? null : request.getOrderedIds()), SubmissionIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    private List<Long> readOrderedIds(List<String> sourceList) {
        List<String> orderedIdValues = RequestListHelper.present(sourceList);
        if (sourceList == null || orderedIdValues.size() != sourceList.size() || orderedIdValues.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("orderedIds");
        }
        List<Long> orderedIds = orderedIdValues.stream()
                .map(value -> Long.valueOf(value.trim()))
                .collect(Collectors.toList());
        Set<Long> uniqueIds = new HashSet<>(orderedIds);
        if (uniqueIds.size() != orderedIds.size()) {
            throw AdminResponseExceptions.invalidParameter("orderedIds");
        }
        return orderedIds;
    }
}
