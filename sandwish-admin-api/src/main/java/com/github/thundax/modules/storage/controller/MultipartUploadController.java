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
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import com.github.thundax.modules.storage.service.MultipartUploadService;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Api(tags = "存储分片上传")
@RequestMapping(value = "/api/storage/objects/multipart")
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
    @RequestMapping(method = RequestMethod.POST)
    @WrappedApiResponse
    public MultipartUploadSessionResponse init(@Valid @RequestBody MultipartUploadInitRequest request) {
        MultipartUploadSession session = new MultipartUploadSession();
        session.setOwnerType(StorageOwnerType.USER);
        session.setOwnerId(UserAccessHolder.currentUserId());
        session.setBusinessType(request.getBusinessType());
        session.setOriginalFilename(request.getOriginalFilename());
        session.setMimeType(request.getMimeType());
        session.setStorageType(storedObjectStore.type());
        session.setTotalSize(request.getTotalSize());
        session.setPartSize(request.getPartSize());
        return StorageInterfaceAssembler.toMultipartSessionResponse(
                multipartUploadService.initMultipartUpload(session));
    }

    @ApiOperation(value = "上传分片", notes = "storage:storage:edit")
    @HasPermission("storage:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
        @ApiImplicitParam(name = "partNumber", value = "分片序号", paramType = "query", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "etag", value = "分片ETag", paramType = "query", dataTypeClass = String.class),
    })
    @RequestMapping(value = "{uploadId}/parts", method = RequestMethod.POST)
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

        MultipartUploadPart part = new MultipartUploadPart();
        part.setUploadId(uploadId);
        part.setPartNumber(readPartNumber(request));
        part.setEtag(readEtag(request, uploadId, part.getPartNumber(), file));
        part.setSize(file.getSize());
        return StorageInterfaceAssembler.toMultipartPartResponse(multipartUploadService.uploadMultipartPart(part));
    }

    @ApiOperation(value = "完成分片上传", notes = "storage:storage:edit")
    @HasPermission("storage:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "{uploadId}/complete", method = RequestMethod.POST)
    @WrappedApiResponse
    public StorageResponse complete(
            @PathVariable("uploadId") String uploadId, @Valid @RequestBody MultipartUploadCompleteRequest request) {
        StoredObject object = toStoredObject(request);
        return StorageInterfaceAssembler.toResponse(
                multipartUploadService.completeMultipartUpload(uploadId, object), storageConverter);
    }

    @ApiOperation(value = "取消分片上传", notes = "storage:storage:edit")
    @HasPermission("storage:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "{uploadId}", method = RequestMethod.DELETE)
    @WrappedApiResponse
    public Boolean abort(@PathVariable("uploadId") String uploadId) {
        return multipartUploadService.abortMultipartUpload(uploadId) > 0;
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

    private StoredObject toStoredObject(MultipartUploadCompleteRequest request) {
        StoredObject object = new StoredObject();
        object.setStorageType(
                StringUtils.isBlank(request.getStorageType())
                        ? storedObjectStore.type()
                        : StorageType.from(request.getStorageType()));
        object.setBucketName(request.getBucketName());
        object.setObjectKey(request.getObjectKey());
        object.setSize(request.getSize());
        object.setAccessEndpoint(request.getAccessEndpoint());
        return object;
    }
}
