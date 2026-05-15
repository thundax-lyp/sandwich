package com.github.thundax.modules.submission.controller;

import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.modules.storage.assembler.StorageInterfaceAssembler;
import com.github.thundax.modules.storage.controller.response.StorageUploadResponse;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.helper.StorageUploadStreamHelper;
import com.github.thundax.modules.submission.assembler.SubmissionInterfaceAssembler;
import com.github.thundax.modules.submission.controller.request.SubmissionSaveRequest;
import com.github.thundax.modules.submission.controller.response.SubmissionResponse;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.service.SubmissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "提交内容")
@RequestMapping(value = "/api/submission/submission")
@WrappedApiController
public class SubmissionController {

    private final SubmissionService submissionService;
    private final StorageUploadStreamHelper storageUploadStreamHelper;
    private final SandwishProperties.UploadProperties uploadProperties;

    public SubmissionController(
            SubmissionService submissionService,
            StorageUploadStreamHelper storageUploadStreamHelper,
            SandwishProperties properties) {
        this.submissionService = submissionService;
        this.storageUploadStreamHelper = storageUploadStreamHelper;
        this.uploadProperties = properties.getUpload();
    }

    @ApiOperation(value = "创建提交内容", notes = "submission:submission:create")
    @HasPermission("submission:submission:create")
    @PostMapping(value = "create")
    public SubmissionResponse create(@Valid @RequestBody SubmissionSaveRequest request) {
        SubmissionId id = submissionService.create(SubmissionInterfaceAssembler.toCreateCommand(request));
        return SubmissionInterfaceAssembler.toResponse(submissionService.get(id));
    }

    @ApiOperation(value = "上传提交图片", notes = "submission:submission:image:upload")
    @HasPermission("submission:submission:image:upload")
    @PostMapping(value = "image/upload")
    public StorageUploadResponse uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return StorageInterfaceAssembler.toUploadResponse(
                storageUploadStreamHelper.upload(
                        file == null ? null : file.getInputStream(),
                        file == null ? null : file.getOriginalFilename(),
                        file == null ? null : file.getContentType(),
                        file == null ? 0L : file.getSize(),
                        allowedImageSuffixes(),
                        StorageOwnerType.SUBMISSION,
                        null),
                uploadProperties.getContentPath());
    }

    private List<String> allowedImageSuffixes() {
        return uploadProperties.getAllowImageSuffix().isEmpty()
                ? uploadProperties.getAllowSuffix()
                : uploadProperties.getAllowImageSuffix();
    }
}
