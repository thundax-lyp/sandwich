package com.github.thundax.modules.assist.controller;

import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.web.request.RequestListHelper;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.assist.assembler.StorageInterfaceAssembler;
import com.github.thundax.modules.assist.controller.request.StorageIdRequest;
import com.github.thundax.modules.assist.controller.request.StoragePageRequest;
import com.github.thundax.modules.assist.controller.response.StorageResponse;
import com.github.thundax.modules.assist.controller.response.StorageTreeNodeResponse;
import com.github.thundax.modules.assist.controller.response.StorageUploadResponse;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.storage.backend.StorageBackend;
import com.github.thundax.modules.storage.backend.StorageBackendObject;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import com.github.thundax.modules.storage.utils.StorageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Api(tags = "08-03.辅助-存储")
@RequestMapping(value = "/api/assist/storage")
@RestController
public class StorageController {

    private final VltavaProperties.UploadProperties properties;
    private final StorageService storageService;
    private final StorageConverter storageConverter;
    private final StorageBackend storageBackend;

    @Autowired
    public StorageController(
            VltavaProperties properties,
            StorageService storageService,
            StorageConverter storageConverter,
            StorageBackend storageBackend) {
        this.properties = properties.getUpload();
        this.storageService = storageService;
        this.storageConverter = storageConverter;
        this.storageBackend = storageBackend;
    }

    @ApiOperation(value = "分页查询存储资源", notes = "assist:storage:view")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "page", method = RequestMethod.POST)
    public PageResponse<StorageResponse> page(@Valid @RequestBody StoragePageRequest request) throws ApiException {
        StorageQuery query = StorageInterfaceAssembler.toQuery(request);
        Page<Storage> page = readStoragePage(request);
        return PageResponseHelper.fromEntityPage(
                storageService.page(query, page),
                storage -> StorageInterfaceAssembler.toResponse(storage, storageConverter));
    }

    @ApiOperation(value = "上传存储资源", notes = "assist:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "upload", method = RequestMethod.POST)
    public StorageUploadResponse upload(HttpServletRequest request) {
        if (!(request instanceof MultipartHttpServletRequest)) {
            return StorageInterfaceAssembler.toUploadErrorResponse("错误的请求格式");
        }

        Map<String, MultipartFile> fileMap = ((MultipartHttpServletRequest) request).getFileMap();
        StorageUploadResponse response = new StorageUploadResponse();
        for (MultipartFile file : fileMap.values()) {
            StorageUploadResponse validatedResponse = validateUploadFile(file);
            if (validatedResponse.getError() != null) {
                return validatedResponse;
            }

            Storage storage = new Storage();
            storage.setOwnerType(StorageOwnerType.USER);
            storage.setOwnerId(UserAccessHolder.currentUserId());
            StorageUtils.applyFileMetadata(file, storage);
            try {
                applyBackendObject(storage, storageBackend.save(storage, file.getInputStream()));
            } catch (IOException e) {
                return StorageInterfaceAssembler.toUploadErrorResponse(e.getMessage());
            }
            storageService.add(storage);
            response = StorageInterfaceAssembler.toUploadResponse(storage, storageConverter);
        }
        return response;
    }

    @ApiOperation(value = "预览存储资源", notes = "assist:storage:view")
    @RequestMapping(value = "file/{id}.{extendName}", method = RequestMethod.GET)
    public void preview(
            @PathVariable("id") String id, @PathVariable("extendName") String extendName, HttpServletResponse response)
            throws IOException {
        Storage storage = storageService.getById(EntityIdCodec.toDomain(id));
        if (storage == null || !StringUtils.equalsAnyIgnoreCase(storage.getExtendName(), extendName)) {
            response.sendError(HttpStatus.SC_NOT_FOUND);
            return;
        }
        if (!storageService.canAccess(storage, StorageOwnerType.USER, UserAccessHolder.currentUserId())) {
            response.sendError(HttpStatus.SC_FORBIDDEN);
            return;
        }

        if (!storageBackend.exists(storage)) {
            response.sendError(HttpStatus.SC_NOT_FOUND);
            return;
        }

        response.setContentType(storage.getMimeType());
        try (InputStream inputStream = storageBackend.open(storage);
                OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, readBytes);
            }
        }
    }

    @ApiOperation(value = "删除存储资源", notes = "assist:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public Boolean delete(@Valid @RequestBody List<StorageIdRequest> list) throws ApiException {
        List<Storage> storageList = new ArrayList<>();
        for (StorageIdRequest request : RequestListHelper.present(list)) {
            Storage storage = storageService.getById(EntityIdCodec.toDomain(request.getId()));
            if (storage == null) {
                throw new NullBeanException("Storage", request.getId());
            }
            storageList.add(storage);
        }
        if (storageList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        storageService.batchDeleteById(storageList.stream().map(Storage::getId).collect(Collectors.toList()));
        return true;
    }

    @ApiOperation(value = "获取业务类型树", notes = "assist:storage:view")
    @RequestMapping(value = "treeData", method = RequestMethod.POST)
    public List<StorageTreeNodeResponse> treeData() {
        return storageService.listBusinessTypes().stream()
                .map(StorageInterfaceAssembler::toBusinessTypeTreeNode)
                .collect(Collectors.toList());
    }

    private StorageUploadResponse validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return StorageInterfaceAssembler.toUploadErrorResponse("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));
        if (!properties.getAllowSuffix().contains(extendName)) {
            return StorageInterfaceAssembler.toUploadErrorResponse("无效的后缀名");
        }
        return new StorageUploadResponse();
    }

    private Page<Storage> readStoragePage(StoragePageRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();

        if (pageNo == null || pageNo < Page.FIRST_PAGE_INDEX) {
            pageNo = Page.FIRST_PAGE_INDEX;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = Page.DEFAULT_PAGE_SIZE;
        }

        Page<Storage> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }

    private void applyBackendObject(Storage storage, StorageBackendObject object) {
        storage.setStorageType(object.getStorageType());
        storage.setBucketName(object.getBucketName());
        storage.setObjectKey(object.getObjectKey());
        storage.setSize(object.getSize());
        storage.setAccessEndpoint(object.getAccessEndpoint());
    }
}
