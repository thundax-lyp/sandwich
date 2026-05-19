package com.github.thundax.modules.storage.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.configure.SandwishProperties;
import com.github.thundax.modules.storage.controller.request.StoragePageRequest;
import com.github.thundax.modules.storage.controller.response.StorageResponse;
import com.github.thundax.modules.storage.controller.response.StorageUploadResponse;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.helper.StorageUploadRequestHelper;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class StorageControllerContractTest {

    @Test
    public void shouldNormalizePageRequestBeforeCallingService() throws Exception {
        StorageService storageService = mock(StorageService.class);
        StorageController controller =
                controller(storageService, mock(StorageConverter.class), mock(StoredObjectStore.class));
        when(storageService.page(any(StorageQuery.class), any(PageQuery.class))).thenAnswer(invocation -> {
            PageQuery page = invocation.getArgument(1);
            return PageResult.of(page.getPageNo(), page.getPageSize(), 0, Collections.emptyList());
        });

        StoragePageRequest request = new StoragePageRequest();
        request.setPageNo(0);
        request.setPageSize(0);
        request.setContentType("image/png");
        request.setObjectStatus("ACTIVE");
        request.setReferenceStatus("REFERENCED");
        request.setOriginalFilename("avatar");
        request.setRemarks("profile");

        PageResponse<StorageResponse> response = controller.page(request);

        ArgumentCaptor<StorageQuery> queryCaptor = ArgumentCaptor.forClass(StorageQuery.class);
        ArgumentCaptor<PageQuery> pageCaptor = ArgumentCaptor.forClass(PageQuery.class);
        verify(storageService).page(queryCaptor.capture(), pageCaptor.capture());
        assertEquals(PageRules.firstPageIndex(), pageCaptor.getValue().getPageNo());
        assertEquals(PageRules.defaultPageSize(), pageCaptor.getValue().getPageSize());
        assertEquals(PageRules.firstPageIndex(), response.getPageNo());
        assertEquals(PageRules.defaultPageSize(), response.getPageSize());
        assertEquals("image/png", queryCaptor.getValue().getContentType());
        assertEquals("avatar", queryCaptor.getValue().getOriginalFilename());
        assertEquals("profile", queryCaptor.getValue().getRemarks());
    }

    @Test
    public void shouldWrapPageJsonResponseWithApiResponseAdvice() throws Exception {
        StorageService storageService = mock(StorageService.class);
        StoredObject storage = storage(1001L);
        storage.setOriginalFilename("profile.png");
        storage.setContentType("image/png");
        when(storageService.page(any(StorageQuery.class), any(PageQuery.class)))
                .thenReturn(PageResult.of(1, 10, 1L, Collections.singletonList(storage)));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                        controller(storageService, mock(StorageConverter.class), mock(StoredObjectStore.class)))
                .setControllerAdvice(new ApiResponseBodyAdvice())
                .build();

        mockMvc.perform(post("/api/storage/object/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNo\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value("1001"))
                .andExpect(jsonPath("$.data.records[0].originalFilename").value("profile.png"));
    }

    @Test
    public void shouldRejectNonMultipartUpload() {
        StorageController controller =
                controller(mock(StorageService.class), mock(StorageConverter.class), mock(StoredObjectStore.class));

        StorageUploadResponse response = controller.upload(new MockHttpServletRequest());

        assertEquals("错误的请求格式", response.getError());
    }

    @Test
    public void shouldReturnNotFoundWhenContentObjectMissing() throws Exception {
        StorageService storageService = mock(StorageService.class);
        StorageController controller =
                controller(storageService, mock(StorageConverter.class), mock(StoredObjectStore.class));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content(404L, response);

        assertEquals(404, response.getStatus());
        verify(storageService).get(any(StoredObjectId.class));
    }

    @Test
    public void shouldReturnNotFoundWhenContentObjectDisabled() throws Exception {
        StoredObject storage = storage(1001L);
        storage.setObjectStatus(com.github.thundax.modules.storage.entity.enums.StoredObjectStatus.DELETED);
        StorageService storageService = mock(StorageService.class);
        when(storageService.get(any(StoredObjectId.class))).thenReturn(storage);
        StorageController controller =
                controller(storageService, mock(StorageConverter.class), mock(StoredObjectStore.class));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content(1001L, response);

        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldStreamContentWhenReadable() throws Exception {
        StoredObject storage = storage(1001L);
        StorageService storageService = mock(StorageService.class);
        StoredObjectStore objectStore = mock(StoredObjectStore.class);
        when(storageService.get(any(StoredObjectId.class))).thenReturn(storage);
        when(objectStore.exists(storage)).thenReturn(true);
        when(objectStore.open(storage)).thenReturn(new ByteArrayInputStream("hello".getBytes("UTF-8")));
        StorageController controller = controller(storageService, mock(StorageConverter.class), objectStore);
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.content(1001L, response);

        assertEquals(200, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals("hello", response.getContentAsString());
    }

    private StorageController controller(
            StorageService storageService, StorageConverter storageConverter, StoredObjectStore objectStore) {
        SandwishProperties properties = new SandwishProperties();
        SandwishProperties.UploadProperties upload = new SandwishProperties.UploadProperties();
        upload.setAllowSuffix(Collections.singletonList("txt"));
        properties.setUpload(upload);
        return new StorageController(
                storageService,
                storageConverter,
                objectStore,
                new StorageUploadRequestHelper(properties, storageService, storageConverter, objectStore));
    }

    private StoredObject storage(Long id) {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectIdCodec.toDomain(id));
        storage.setMimeType("text/plain");
        storage.setName("note");
        storage.setExtendName("txt");
        return storage;
    }
}
