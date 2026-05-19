package com.github.thundax.modules.storage.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.configure.SandwishProperties;
import com.github.thundax.modules.storage.controller.response.StorageUploadResponse;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.command.CreateStorageCommand;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import java.io.InputStream;
import java.util.Collections;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

public class StorageUploadRequestHelperTest {

    @Test
    public void shouldRejectNonMultipartRequest() {
        StorageUploadRequestHelper helper =
                helper(mock(StorageService.class), mock(StorageConverter.class), mock(StoredObjectStore.class));

        StorageUploadResponse response = helper.upload(new MockHttpServletRequest(), StorageOwnerType.USER, "1001");

        assertEquals("错误的请求格式", response.getError());
    }

    @Test
    public void shouldUploadSubmissionFileWithSubmissionOwnerType() throws Exception {
        StorageService storageService = mock(StorageService.class);
        StorageConverter storageConverter = mock(StorageConverter.class);
        StoredObjectStore storedObjectStore = mock(StoredObjectStore.class);
        StoredObject storedObject = new StoredObject();
        storedObject.setBucketName("bucket");
        storedObject.setObjectKey("submission/image.png");
        storedObject.setSize(12L);
        storedObject.setAccessEndpoint("/files/submission/image.png");
        when(storedObjectStore.save(any(StoredObject.class), any(InputStream.class)))
                .thenReturn(storedObject);
        when(storageService.create(any(CreateStorageCommand.class))).thenReturn(StoredObjectIdCodec.toDomain(3001L));
        when(storageConverter.toPreviewUrl(any(StoredObject.class))).thenReturn("/api/storage/object/3001/content");
        StorageUploadRequestHelper helper = helper(storageService, storageConverter, storedObjectStore);

        StorageUploadResponse response = helper.upload(multipartRequest(), StorageOwnerType.SUBMISSION, null);

        assertEquals("3001", response.getId());
        assertEquals("image.png", response.getOriginalFilename());
        assertEquals("png", response.getExtendName());
        assertEquals("image/png", response.getContentType());
        assertEquals("/api/storage/object/3001/content", response.getContentUrl());
        assertNull(response.getError());
        ArgumentCaptor<CreateStorageCommand> captor = ArgumentCaptor.forClass(CreateStorageCommand.class);
        verify(storageService).create(captor.capture());
        assertEquals(StorageOwnerType.SUBMISSION, captor.getValue().getOwnerType());
        assertNull(captor.getValue().getOwnerId());
        assertEquals("bucket", captor.getValue().getBucketName());
        assertEquals("submission/image.png", captor.getValue().getObjectKey());
    }

    private StorageUploadRequestHelper helper(
            StorageService storageService, StorageConverter storageConverter, StoredObjectStore storedObjectStore) {
        SandwishProperties properties = new SandwishProperties();
        SandwishProperties.UploadProperties upload = new SandwishProperties.UploadProperties();
        upload.setAllowSuffix(Collections.singletonList("png"));
        properties.setUpload(upload);
        return new StorageUploadRequestHelper(properties, storageService, storageConverter, storedObjectStore);
    }

    private MockMultipartHttpServletRequest multipartRequest() {
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.addFile(new MockMultipartFile("file", "image.png", "image/png", "image-bytes".getBytes()));
        return request;
    }
}
