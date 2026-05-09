package com.github.thundax.modules.storage.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.web.annotation.WrappedApiResponse;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.storage.assembler.StorageInterfaceAssembler;
import com.github.thundax.modules.storage.controller.request.MultipartUploadCompleteRequest;
import com.github.thundax.modules.storage.controller.request.MultipartUploadInitRequest;
import com.github.thundax.modules.storage.controller.response.MultipartUploadPartResponse;
import com.github.thundax.modules.storage.controller.response.MultipartUploadSessionResponse;
import com.github.thundax.modules.storage.controller.response.StorageResponse;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import com.github.thundax.modules.storage.service.MultipartUploadService;
import com.github.thundax.modules.storage.service.command.AbortMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.CompleteMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.InitMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.UploadMultipartPartCommand;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "存储分片上传")
@RequestMapping(value = "/api/storage/multipart-upload")
@RestController
public class MultipartUploadController {

    private final MultipartUploadService multipartUploadService;
    private final StoredObjectStore storedObjectStore;
    private final StorageConverter storageConverter;

    @Autowired
    public MultipartUploadController(
            MultipartUploadService multipartUploadService,
            StoredObjectStore storedObjectStore,
            StorageConverter storageConverter) {
        this.multipartUploadService = multipartUploadService;
        this.storedObjectStore = storedObjectStore;
        this.storageConverter = storageConverter;
    }

    @ApiOperation(value = "初始化分片上传", notes = "storage:storage:edit")
    @HasPermission("storage:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @PostMapping
    @WrappedApiResponse
    public MultipartUploadSessionResponse init(@Valid @RequestBody MultipartUploadInitRequest request) {
        return StorageInterfaceAssembler.toMultipartSessionResponse(
                multipartUploadService.init(toInitMultipartUploadCommand(request)));
    }

    @ApiOperation(value = "上传分片", notes = "storage:storage:edit")
    @HasPermission("storage:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
        @ApiImplicitParam(name = "partNumber", value = "分片序号", paramType = "query", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "etag", value = "分片ETag", paramType = "query", dataTypeClass = String.class),
    })
    @PostMapping(value = "{uploadId}/parts")
    @WrappedApiResponse
    public MultipartUploadPartResponse uploadPart(@PathVariable("uploadId") String uploadId, HttpServletRequest request)
            throws ApiException {
        if (!(request instanceof MultipartHttpServletRequest)) {
            throw new InvalidParameterException("file");
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFileMap().values().stream()
                .findFirst()
                .orElseThrow(() -> new InvalidParameterException("file"));
        if (file.isEmpty()) {
            throw new InvalidParameterException("file");
        }

        Integer partNumber = readPartNumber(request);
        UploadMultipartPartCommand command = new UploadMultipartPartCommand();
        command.setUploadId(uploadId);
        command.setPartNumber(partNumber);
        command.setEtag(readEtag(request, uploadId, partNumber, file));
        command.setSize(file.getSize());
        return StorageInterfaceAssembler.toMultipartPartResponse(multipartUploadService.uploadPart(command));
    }

    @ApiOperation(value = "完成分片上传", notes = "storage:storage:edit")
    @HasPermission("storage:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @PostMapping(value = "{uploadId}/complete")
    @WrappedApiResponse
    public StorageResponse complete(
            @PathVariable("uploadId") String uploadId, @Valid @RequestBody MultipartUploadCompleteRequest request) {
        return StorageInterfaceAssembler.toResponse(
                multipartUploadService.complete(toCompleteMultipartUploadCommand(uploadId, request)), storageConverter);
    }

    @ApiOperation(value = "取消分片上传", notes = "storage:storage:edit")
    @HasPermission("storage:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @PostMapping(value = "{uploadId}/abort")
    @WrappedApiResponse
    public Boolean abort(@PathVariable("uploadId") String uploadId) {
        return multipartUploadService.abort(new AbortMultipartUploadCommand(uploadId)) > 0;
    }

    private Integer readPartNumber(HttpServletRequest request) throws ApiException {
        String partNumber = request.getParameter("partNumber");
        if (StringUtils.isBlank(partNumber)) {
            throw new InvalidParameterException("partNumber");
        }
        try {
            return Integer.valueOf(partNumber);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("partNumber");
        }
    }

    private String readEtag(HttpServletRequest request, String uploadId, Integer partNumber, MultipartFile file) {
        String etag = request.getParameter("etag");
        if (StringUtils.isNotBlank(etag)) {
            return etag;
        }
        if (StringUtils.isNotBlank(file.getOriginalFilename())) {
            return file.getOriginalFilename();
        }
        return uploadId + "-" + partNumber;
    }

    private InitMultipartUploadCommand toInitMultipartUploadCommand(MultipartUploadInitRequest request) {
        InitMultipartUploadCommand command = new InitMultipartUploadCommand();
        command.setOwnerType(StorageOwnerType.USER);
        command.setOwnerId(UserAccessHolder.currentUserId());
        command.setBusinessType(request.getBusinessType());
        command.setOriginalFilename(request.getOriginalFilename());
        command.setMimeType(request.getMimeType());
        command.setStorageType(storedObjectStore.type());
        command.setTotalSize(request.getTotalSize());
        command.setPartSize(request.getPartSize());
        return command;
    }

    private CompleteMultipartUploadCommand toCompleteMultipartUploadCommand(
            String uploadId, MultipartUploadCompleteRequest request) {
        CompleteMultipartUploadCommand command = new CompleteMultipartUploadCommand();
        command.setUploadId(uploadId);
        command.setStorageType(
                StringUtils.isBlank(request.getStorageType())
                        ? storedObjectStore.type()
                        : StorageType.from(request.getStorageType()));
        command.setBucketName(request.getBucketName());
        command.setObjectKey(request.getObjectKey());
        command.setSize(request.getSize());
        command.setAccessEndpoint(request.getAccessEndpoint());
        return command;
    }
}
