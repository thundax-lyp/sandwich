package com.github.thundax.modules.storage.helper;

import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.modules.storage.assembler.StorageInterfaceAssembler;
import com.github.thundax.modules.storage.controller.response.StorageUploadResponse;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.command.CreateStorageCommand;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import com.github.thundax.modules.storage.utils.StorageUtils;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Component
public class StorageUploadRequestHelper {

    private final SandwishProperties.UploadProperties properties;
    private final StorageService storageService;
    private final StorageConverter storageConverter;
    private final StoredObjectStore storedObjectStore;

    public StorageUploadRequestHelper(
            SandwishProperties properties,
            StorageService storageService,
            StorageConverter storageConverter,
            StoredObjectStore storedObjectStore) {
        this.properties = properties.getUpload();
        this.storageService = storageService;
        this.storageConverter = storageConverter;
        this.storedObjectStore = storedObjectStore;
    }

    public StorageUploadResponse upload(HttpServletRequest request, StorageOwnerType ownerType, String ownerId) {
        if (!(request instanceof MultipartHttpServletRequest)) {
            return StorageInterfaceAssembler.toUploadErrorResponse("错误的请求格式");
        }

        Map<String, MultipartFile> fileMap = ((MultipartHttpServletRequest) request).getFileMap();
        StorageUploadResponse response = StorageInterfaceAssembler.toUploadEmptyResponse();
        for (MultipartFile file : fileMap.values()) {
            StorageUploadResponse validatedResponse = validateUploadFile(file);
            if (validatedResponse.getError() != null) {
                return validatedResponse;
            }
            response = uploadFile(file, ownerType, ownerId);
            if (response.getError() != null) {
                return response;
            }
        }
        return response;
    }

    private StorageUploadResponse uploadFile(MultipartFile file, StorageOwnerType ownerType, String ownerId) {
        StoredObject storage = new StoredObject();
        storage.setOwnerType(ownerType);
        storage.setOwnerId(ownerId);
        StorageUtils.applyFileMetadata(file, storage);
        try {
            applyStoredObject(storage, storedObjectStore.save(storage, file.getInputStream()));
        } catch (IOException e) {
            return StorageInterfaceAssembler.toUploadErrorResponse(e.getMessage());
        }
        storage.setId(storageService.create(toCreateStorageCommand(storage)));
        return StorageInterfaceAssembler.toUploadResponse(storage, storageConverter);
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
        return StorageInterfaceAssembler.toUploadEmptyResponse();
    }

    private void applyStoredObject(StoredObject storage, StoredObject object) {
        storage.setBucketName(object.getBucketName());
        storage.setObjectKey(object.getObjectKey());
        storage.setSize(object.getSize());
        storage.setAccessEndpoint(object.getAccessEndpoint());
    }

    private CreateStorageCommand toCreateStorageCommand(StoredObject storage) {
        CreateStorageCommand command = new CreateStorageCommand();
        command.setId(storage.getId());
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeType());
        command.setOwnerId(storage.getOwnerId());
        command.setOwnerType(storage.getOwnerType());
        command.setBucketName(storage.getBucketName());
        command.setObjectKey(storage.getObjectKey());
        command.setSize(storage.getSize());
        command.setAccessEndpoint(storage.getAccessEndpoint());
        command.setObjectStatus(storage.getObjectStatus());
        command.setReferenceStatus(storage.getReferenceStatus());
        command.setRemarks(storage.getRemarks());
        return command;
    }
}
