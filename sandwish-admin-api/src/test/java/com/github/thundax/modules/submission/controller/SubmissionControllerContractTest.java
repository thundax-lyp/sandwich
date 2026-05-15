package com.github.thundax.modules.submission.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.modules.storage.controller.response.StorageUploadResponse;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.helper.StorageUploadRequestHelper;
import com.github.thundax.modules.submission.controller.request.SubmissionPageRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionSaveRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionSortRequest;
import com.github.thundax.modules.submission.controller.request.SubmissionStatusRequest;
import com.github.thundax.modules.submission.controller.response.SubmissionResponse;
import com.github.thundax.modules.submission.entity.Submission;
import com.github.thundax.modules.submission.entity.SubmissionImage;
import com.github.thundax.modules.submission.entity.enums.SubmissionStatus;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionIdCodec;
import com.github.thundax.modules.submission.service.SubmissionService;
import com.github.thundax.modules.submission.service.command.ChangeSubmissionStatusCommand;
import com.github.thundax.modules.submission.service.command.CreateSubmissionCommand;
import com.github.thundax.modules.submission.service.command.SubmissionSortCommand;
import com.github.thundax.modules.submission.service.query.SubmissionQuery;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class SubmissionControllerContractTest {

    @Test
    public void shouldNormalizePageRequestBeforeCallingService() {
        SubmissionService submissionService = mock(SubmissionService.class);
        SubmissionController controller =
                new SubmissionController(submissionService, mock(StorageUploadRequestHelper.class));
        when(submissionService.page(any(SubmissionQuery.class), any(PageQuery.class)))
                .thenAnswer(invocation -> {
                    PageQuery page = invocation.getArgument(1);
                    return PageResult.of(page.getPageNo(), page.getPageSize(), 0, Collections.emptyList());
                });

        SubmissionPageRequest request = new SubmissionPageRequest();
        request.setPageNo(0);
        request.setPageSize(0);
        request.setStatus("SUBMITTED");
        request.setSortDirection(SortDirection.DESC);

        PageResponse<SubmissionResponse> response = controller.page(request);

        ArgumentCaptor<SubmissionQuery> queryCaptor = ArgumentCaptor.forClass(SubmissionQuery.class);
        ArgumentCaptor<PageQuery> pageCaptor = ArgumentCaptor.forClass(PageQuery.class);
        verify(submissionService).page(queryCaptor.capture(), pageCaptor.capture());
        assertEquals(PageRules.firstPageIndex(), pageCaptor.getValue().getPageNo());
        assertEquals(PageRules.defaultPageSize(), pageCaptor.getValue().getPageSize());
        assertEquals(PageRules.firstPageIndex(), response.getPageNo());
        assertEquals(PageRules.defaultPageSize(), response.getPageSize());
        assertEquals(SubmissionStatus.SUBMITTED, queryCaptor.getValue().getStatus());
        assertEquals(SortDirection.DESC, queryCaptor.getValue().getSortDirection());
    }

    @Test
    public void shouldWrapPageJsonResponseWithApiResponseAdvice() throws Exception {
        SubmissionService submissionService = mock(SubmissionService.class);
        when(submissionService.page(any(SubmissionQuery.class), any(PageQuery.class)))
                .thenReturn(PageResult.of(1, 10, 1L, Collections.singletonList(submission(1001L))));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                        new SubmissionController(submissionService, mock(StorageUploadRequestHelper.class)))
                .setControllerAdvice(new ApiResponseBodyAdvice())
                .build();

        mockMvc.perform(post("/api/submission/submission/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNo\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value("1001"))
                .andExpect(jsonPath("$.data.records[0].imageObjectIds[0]").value("2001"));
    }

    @Test
    public void shouldConvertStatusRequestToCommand() {
        SubmissionService submissionService = mock(SubmissionService.class);
        SubmissionController controller =
                new SubmissionController(submissionService, mock(StorageUploadRequestHelper.class));
        SubmissionStatusRequest request = new SubmissionStatusRequest();
        request.setId("1001");
        request.setStatus("APPROVED");

        assertTrue(controller.changeStatus(request));

        ArgumentCaptor<ChangeSubmissionStatusCommand> captor =
                ArgumentCaptor.forClass(ChangeSubmissionStatusCommand.class);
        verify(submissionService).changeStatus(captor.capture());
        assertEquals(Long.valueOf(1001L), captor.getValue().getId().value());
        assertEquals(SubmissionStatus.APPROVED, captor.getValue().getStatus());
    }

    @Test
    public void shouldConvertCreateRequestToCommandAndReturnCreatedSubmission() {
        SubmissionService submissionService = mock(SubmissionService.class);
        SubmissionId submissionId = SubmissionId.of(1001L);
        when(submissionService.create(any(CreateSubmissionCommand.class))).thenReturn(submissionId);
        when(submissionService.get(submissionId)).thenReturn(submission(1001L));
        SubmissionController controller =
                new SubmissionController(submissionService, mock(StorageUploadRequestHelper.class));
        SubmissionSaveRequest request = new SubmissionSaveRequest();
        request.setTitle("Title");
        request.setContent("Content");
        request.setImageObjectIds(Collections.singletonList("2001"));

        SubmissionResponse response = controller.create(request);

        ArgumentCaptor<CreateSubmissionCommand> captor = ArgumentCaptor.forClass(CreateSubmissionCommand.class);
        verify(submissionService).create(captor.capture());
        assertEquals("Title", captor.getValue().getTitle());
        assertEquals("Content", captor.getValue().getContent());
        assertEquals(
                StoredObjectIdCodec.toDomain(2001L),
                captor.getValue().getImageObjectIds().get(0));
        assertEquals("1001", response.getId());
    }

    @Test
    public void shouldConvertSortRequestToCommand() {
        SubmissionService submissionService = mock(SubmissionService.class);
        SubmissionController controller =
                new SubmissionController(submissionService, mock(StorageUploadRequestHelper.class));
        SubmissionSortRequest request = new SubmissionSortRequest();
        request.setOrderedIds(Arrays.asList("1001", "1002"));
        request.setSortDirection(SortDirection.ASC);

        assertTrue(controller.sort(request));

        ArgumentCaptor<SubmissionSortCommand> captor = ArgumentCaptor.forClass(SubmissionSortCommand.class);
        verify(submissionService).sort(captor.capture());
        assertEquals(
                Long.valueOf(1001L), captor.getValue().getOrderedIds().get(0).value());
        assertEquals(
                Long.valueOf(1002L), captor.getValue().getOrderedIds().get(1).value());
        assertEquals(SortDirection.ASC, captor.getValue().getSortDirection());
    }

    @Test
    public void shouldDeleteSubmissionIds() {
        SubmissionService submissionService = mock(SubmissionService.class);
        SubmissionController controller =
                new SubmissionController(submissionService, mock(StorageUploadRequestHelper.class));
        com.github.thundax.modules.submission.controller.request.SubmissionIdRequest request =
                new com.github.thundax.modules.submission.controller.request.SubmissionIdRequest();
        request.setId("1001");

        assertTrue(controller.delete(Collections.singletonList(request)));

        verify(submissionService).remove(SubmissionId.of(1001L));
    }

    @Test
    public void shouldDelegateImageUploadWithSubmissionOwnerType() {
        SubmissionService submissionService = mock(SubmissionService.class);
        StorageUploadRequestHelper storageUploadRequestHelper = mock(StorageUploadRequestHelper.class);
        StorageUploadResponse uploadResponse =
                StorageUploadResponse.builder().id("3001").build();
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(storageUploadRequestHelper.upload(any(HttpServletRequest.class), any(StorageOwnerType.class), any()))
                .thenReturn(uploadResponse);
        SubmissionController controller = new SubmissionController(submissionService, storageUploadRequestHelper);

        StorageUploadResponse response = controller.uploadImage(request);

        assertEquals("3001", response.getId());
        verify(storageUploadRequestHelper).upload(request, StorageOwnerType.SUBMISSION, null);
    }

    private Submission submission(Long id) {
        Submission submission = new Submission();
        SubmissionId submissionId = SubmissionIdCodec.toDomain(id);
        submission.setId(submissionId);
        submission.setTitle("Title");
        submission.setContent("Content");
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setImages(Collections.singletonList(
                new SubmissionImage(null, submissionId, StoredObjectIdCodec.toDomain(2001L), 0)));
        return submission;
    }
}
