package com.github.thundax.modules.storage.controller;

import com.github.thundax.common.exception.AdminResponseExceptions;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.token.AccessTokenNames;
import com.github.thundax.common.web.annotation.WrappedApiResponse;
import com.github.thundax.common.web.assembler.PageInterfaceAssembler;
import com.github.thundax.common.web.request.RequestListHelper;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.storage.assembler.StorageInterfaceAssembler;
import com.github.thundax.modules.storage.controller.request.StorageIdRequest;
import com.github.thundax.modules.storage.controller.request.StoragePageRequest;
import com.github.thundax.modules.storage.controller.request.StorageSortRequest;
import com.github.thundax.modules.storage.controller.response.StorageResponse;
import com.github.thundax.modules.storage.controller.response.StorageTreeNodeResponse;
import com.github.thundax.modules.storage.controller.response.StorageUploadResponse;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.helper.StorageUploadRequestHelper;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.command.StorageSortCommand;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "存储")
@RequestMapping(value = "/api/storage/object")
@RestController
public class StorageController {

    private final StorageService storageService;
    private final StorageConverter storageConverter;
    private final StoredObjectStore storedObjectStore;
    private final StorageUploadRequestHelper storageUploadRequestHelper;

    @Autowired
    public StorageController(
            StorageService storageService,
            StorageConverter storageConverter,
            StoredObjectStore storedObjectStore,
            StorageUploadRequestHelper storageUploadRequestHelper) {
        this.storageService = storageService;
        this.storageConverter = storageConverter;
        this.storedObjectStore = storedObjectStore;
        this.storageUploadRequestHelper = storageUploadRequestHelper;
    }

    @ApiOperation(value = "分页查询存储资源", notes = "storage:storage:view")
    @HasPermission("storage:storage:view")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @PostMapping(value = "page")
    @WrappedApiResponse
    public PageResponse<StorageResponse> page(@Valid @RequestBody StoragePageRequest request) {
        StorageQuery query = StorageInterfaceAssembler.toQuery(request);
        return PageResponseHelper.fromPageResult(
                storageService.page(query, PageInterfaceAssembler.toPageQuery(request)),
                storage -> StorageInterfaceAssembler.toResponse(storage, storageConverter));
    }

    @ApiOperation(value = "上传存储资源", notes = "storage:storage:edit")
    @HasPermission("storage:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @PostMapping(value = "upload")
    @WrappedApiResponse
    public StorageUploadResponse upload(HttpServletRequest request) {
        return storageUploadRequestHelper.upload(
                request, StorageOwnerType.USER, SandwishContextHolder.currentSubjectId());
    }

    @ApiOperation(value = "读取存储对象内容", notes = "storage:storage:view")
    @HasPermission("storage:storage:view")
    @GetMapping(value = "{id}/content")
    public void content(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        StoredObject storage = storageService.get(StoredObjectIdCodec.toDomain(id));
        if (storage == null) {
            response.sendError(HttpStatus.SC_NOT_FOUND);
            return;
        }
        if (!storage.isEnable()) {
            response.sendError(HttpStatus.SC_NOT_FOUND);
            return;
        }

        if (!storedObjectStore.exists(storage)) {
            response.sendError(HttpStatus.SC_NOT_FOUND);
            return;
        }

        response.setContentType(storage.getMimeType());
        try (InputStream inputStream = storedObjectStore.open(storage);
                OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, readBytes);
            }
        }
    }

    @ApiOperation(value = "删除存储资源", notes = "storage:storage:edit")
    @HasPermission("storage:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "X-Access-Token", value = "令牌", paramType = "header", dataTypeClass = String.class),
    })
    @PostMapping(value = "delete")
    @WrappedApiResponse
    public Boolean delete(@Valid @RequestBody List<StorageIdRequest> list) {
        List<StoredObject> storageList = new ArrayList<>();
        for (StorageIdRequest request : RequestListHelper.present(list)) {
            StoredObject storage = storageService.get(StoredObjectIdCodec.toDomain(request.getId()));
            if (storage == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            storageList.add(storage);
        }
        if (storageList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        for (StoredObject storage : storageList) {
            storageService.remove(storage.getId());
        }
        return true;
    }

    @ApiOperation(value = "排序", notes = "storage:storage:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("storage:storage:edit")
    @PostMapping(value = "sort")
    @WrappedApiResponse
    public Boolean sort(@Valid @RequestBody StorageSortRequest request) {
        storageService.sort(new StorageSortCommand(
                RequestListHelper.map(
                        readOrderedIds(request == null ? null : request.getOrderedIds()),
                        StoredObjectIdCodec::toDomain),
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

    @ApiOperation(value = "获取业务类型树", notes = "storage:storage:view")
    @HasPermission("storage:storage:view")
    @PostMapping(value = "tree")
    @WrappedApiResponse
    public List<StorageTreeNodeResponse> tree() {
        return storageService.listReferenceOwnerTypes(new StorageQuery()).stream()
                .map(StorageInterfaceAssembler::toBusinessTypeTreeNode)
                .collect(Collectors.toList());
    }
}
