package com.github.thundax.modules.submission.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.configure.SandwishProperties;
import com.github.thundax.modules.auth.security.OpenApiHeaders;
import com.github.thundax.modules.storage.controller.response.StorageUploadResponse;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.storage.helper.StorageUploadResult;
import com.github.thundax.modules.storage.helper.StorageUploadStreamHelper;
import com.github.thundax.modules.submission.controller.request.SubmissionPageRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionSaveRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionStatusRequest;
import com.github.thundax.modules.submission.controller.response.SubmissionResponse;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.SubmissionImage;
import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionImageId;
import com.github.thundax.modules.submission.service.SubmissionService;
import com.github.thundax.modules.submission.service.command.ChangeSubmissionStatusCommand;
import com.github.thundax.modules.submission.service.command.CreateSubmissionCommand;
import com.github.thundax.modules.submission.service.query.SubmissionQuery;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class SubmissionControllerContractTest {

    @Test
    public void shouldConvertCreateRequestToCommand() {
        SubmissionService submissionService = mock(SubmissionService.class);
        StorageUploadStreamHelper uploadStreamHelper = mock(StorageUploadStreamHelper.class);
        SubmissionController controller = new SubmissionController(submissionService, uploadStreamHelper, properties());
        when(submissionService.create(any(CreateSubmissionCommand.class))).thenReturn(SubmissionId.of(9001L));
        when(submissionService.get(SubmissionId.of(9001L))).thenReturn(submission());

        SubmissionResponse response = controller.create(saveRequest());

        ArgumentCaptor<CreateSubmissionCommand> captor = ArgumentCaptor.forClass(CreateSubmissionCommand.class);
        verify(submissionService).create(captor.capture());
        assertEquals("Partner form", captor.getValue().getTitle());
        assertEquals("Hello", captor.getValue().getContent());
        assertEquals(
                Long.valueOf(7001L),
                captor.getValue().getImageObjectIds().get(0).value());
        assertEquals("9001", response.getId());
        assertEquals("SUBMITTED", response.getStatus());
        assertEquals(Collections.singletonList("7001"), response.getImageObjectIds());
    }

    @Test
    public void shouldUploadImageThroughStreamHelper() throws Exception {
        SubmissionService submissionService = mock(SubmissionService.class);
        StorageUploadStreamHelper uploadStreamHelper = mock(StorageUploadStreamHelper.class);
        SubmissionController controller = new SubmissionController(submissionService, uploadStreamHelper, properties());
        when(uploadStreamHelper.upload(
                        any(InputStream.class),
                        eq("demo.png"),
                        eq("image/png"),
                        eq(4L),
                        eq(Collections.singletonList("png")),
                        eq(StorageOwnerType.SUBMISSION),
                        isNull()))
                .thenReturn(StorageUploadResult.builder().storage(storage()).build());

        MockMultipartFile file =
                new MockMultipartFile("file", "demo.png", "image/png", "demo".getBytes(StandardCharsets.UTF_8));
        StorageUploadResponse response = controller.uploadImage(file);

        verify(uploadStreamHelper)
                .upload(
                        any(InputStream.class),
                        eq("demo.png"),
                        eq("image/png"),
                        eq(4L),
                        eq(Collections.singletonList("png")),
                        eq(StorageOwnerType.SUBMISSION),
                        isNull());
        assertEquals("7001", response.getId());
        assertEquals("demo.png", response.getOriginalFilename());
        assertEquals("/open-api/api/storage/object/7001/content", response.getContentUrl());
    }

    @Test
    public void shouldConvertPageRequestToQuery() {
        SubmissionService submissionService = mock(SubmissionService.class);
        StorageUploadStreamHelper uploadStreamHelper = mock(StorageUploadStreamHelper.class);
        SubmissionController controller = new SubmissionController(submissionService, uploadStreamHelper, properties());
        when(submissionService.page(any(SubmissionQuery.class), any(PageQuery.class)))
                .thenReturn(PageResult.of(1, 10, 1L, Collections.singletonList(submission())));

        controller.page(pageRequest());

        ArgumentCaptor<SubmissionQuery> queryCaptor = ArgumentCaptor.forClass(SubmissionQuery.class);
        ArgumentCaptor<PageQuery> pageCaptor = ArgumentCaptor.forClass(PageQuery.class);
        verify(submissionService).page(queryCaptor.capture(), pageCaptor.capture());
        assertEquals("SUBMITTED", queryCaptor.getValue().getStatus().value());
        assertEquals(1, pageCaptor.getValue().getPageNo());
        assertEquals(10, pageCaptor.getValue().getPageSize());
    }

    @Test
    public void shouldConvertStatusRequestToCommand() {
        SubmissionService submissionService = mock(SubmissionService.class);
        StorageUploadStreamHelper uploadStreamHelper = mock(StorageUploadStreamHelper.class);
        SubmissionController controller = new SubmissionController(submissionService, uploadStreamHelper, properties());

        controller.changeStatus(statusRequest());

        ArgumentCaptor<ChangeSubmissionStatusCommand> captor =
                ArgumentCaptor.forClass(ChangeSubmissionStatusCommand.class);
        verify(submissionService).changeStatus(captor.capture());
        assertEquals(Long.valueOf(9001L), Long.valueOf(captor.getValue().getId().value()));
        assertEquals("APPROVED", captor.getValue().getStatus().value());
    }

    @Test
    public void shouldDeclareSubmissionPermissions() throws Exception {
        assertEquals(
                "submission:submission:create",
                permission(SubmissionController.class.getMethod("create", SubmissionSaveRequest.class)));
        assertEquals(
                "submission:submission:page",
                permission(SubmissionController.class.getMethod("page", SubmissionPageRequest.class)));
        assertEquals(
                "submission:submission:change-status",
                permission(SubmissionController.class.getMethod("changeStatus", SubmissionStatusRequest.class)));
        assertEquals(
                "submission:submission:image:upload",
                permission(SubmissionController.class.getMethod("uploadImage", MultipartFile.class)));
    }

    @Test
    public void shouldDeclareOpenApiSwaggerHeaders() throws Exception {
        assertOpenApiHeaders(SubmissionController.class.getMethod("create", SubmissionSaveRequest.class));
        assertOpenApiHeaders(SubmissionController.class.getMethod("page", SubmissionPageRequest.class));
        assertOpenApiHeaders(SubmissionController.class.getMethod("changeStatus", SubmissionStatusRequest.class));
        assertOpenApiHeaders(SubmissionController.class.getMethod("uploadImage", MultipartFile.class));
    }

    private String permission(java.lang.reflect.Method method) {
        return method.getAnnotation(HasPermission.class).value()[0];
    }

    private void assertOpenApiHeaders(Method method) {
        ApiImplicitParams params = method.getAnnotation(ApiImplicitParams.class);
        Set<String> names = new LinkedHashSet<>();
        for (ApiImplicitParam param : params.value()) {
            names.add(param.name());
        }

        assertEquals(true, names.contains(OpenApiHeaders.API_KEY));
        assertEquals(true, names.contains(OpenApiHeaders.TIMESTAMP));
        assertEquals(true, names.contains(OpenApiHeaders.NONCE));
        assertEquals(true, names.contains(OpenApiHeaders.CONTENT_SHA256));
        assertEquals(true, names.contains(OpenApiHeaders.SIGNATURE));
    }

    private SubmissionSaveRequest saveRequest() {
        SubmissionSaveRequest request = new SubmissionSaveRequest();
        request.setTitle("Partner form");
        request.setContent("Hello");
        request.setImageObjectIds(Collections.singletonList("7001"));
        return request;
    }

    private SubmissionPageRequest pageRequest() {
        SubmissionPageRequest request = new SubmissionPageRequest();
        request.setPageNo(1);
        request.setPageSize(10);
        request.setStatus("SUBMITTED");
        return request;
    }

    private SubmissionStatusRequest statusRequest() {
        SubmissionStatusRequest request = new SubmissionStatusRequest();
        request.setId("9001");
        request.setStatus("APPROVED");
        return request;
    }

    private Submission submission() {
        Submission submission = new Submission();
        submission.setId(SubmissionId.of(9001L));
        submission.setTitle("Partner form");
        submission.setContent("Hello");
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setImages(Arrays.asList(
                new SubmissionImage(SubmissionImageId.of(8001L), SubmissionId.of(9001L), StoredObjectId.of(7001L), 1)));
        return submission;
    }

    private StoredObject storage() {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(7001L));
        storage.setOriginalFilename("demo.png");
        storage.setName("demo");
        storage.setExtendName("png");
        storage.setContentType("image/png");
        return storage;
    }

    private SandwishProperties properties() {
        SandwishProperties properties = new SandwishProperties();
        SandwishProperties.UploadProperties upload = new SandwishProperties.UploadProperties();
        upload.setAllowImageSuffix(Collections.singletonList("png"));
        upload.setContentPath("/api/storage/object/");
        properties.setUpload(upload);
        return properties;
    }
}
