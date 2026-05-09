package com.github.thundax.modules.storage.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.controller.request.MultipartUploadCompleteRequest;
import com.github.thundax.modules.storage.controller.request.MultipartUploadInitRequest;
import com.github.thundax.modules.storage.controller.response.MultipartUploadPartResponse;
import com.github.thundax.modules.storage.controller.response.MultipartUploadSessionResponse;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.MultipartUploadStatus;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import com.github.thundax.modules.storage.service.MultipartUploadService;
import com.github.thundax.modules.storage.service.command.AbortMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.CompleteMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.InitMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.UploadMultipartPartCommand;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

public class MultipartUploadControllerContractTest {

    @Test
    public void shouldInitMultipartUploadSessionForCurrentAdminUser() {
        MultipartUploadService service = mock(MultipartUploadService.class);
        StoredObjectStore store = mock(StoredObjectStore.class);
        when(store.type()).thenReturn(StorageType.LOCAL_FILE);
        when(service.init(any(InitMultipartUploadCommand.class))).thenAnswer(invocation -> {
            InitMultipartUploadCommand command = invocation.getArgument(0);
            MultipartUploadSession session = new MultipartUploadSession();
            session.setId(EntityId.of(9301L));
            session.setUploadId("upload-1");
            session.setOwnerType(command.getOwnerType());
            session.setStorageType(command.getStorageType());
            session.setOriginalFilename(command.getOriginalFilename());
            session.setUploadStatus(MultipartUploadStatus.INITIATED);
            return session;
        });
        MultipartUploadController controller = controller(service, store);
        MultipartUploadInitRequest request = new MultipartUploadInitRequest();
        request.setOriginalFilename("demo.png");
        request.setMimeType("image/png");
        request.setTotalSize(300L);
        request.setPartSize(100L);

        MultipartUploadSessionResponse response = controller.init(request);

        ArgumentCaptor<InitMultipartUploadCommand> captor =
                ArgumentCaptor.forClass(InitMultipartUploadCommand.class);
        verify(service).init(captor.capture());
        assertEquals(Long.valueOf(9301L), response.getId());
        assertEquals("upload-1", response.getUploadId());
        assertEquals(StorageOwnerType.USER, captor.getValue().getOwnerType());
        assertEquals(StorageType.LOCAL_FILE, captor.getValue().getStorageType());
        assertEquals("demo.png", captor.getValue().getOriginalFilename());
    }

    @Test
    public void shouldUploadMultipartPartFromMultipartRequest() throws Exception {
        MultipartUploadService service = mock(MultipartUploadService.class);
        when(service.uploadPart(any(UploadMultipartPartCommand.class))).thenAnswer(invocation -> {
            UploadMultipartPartCommand command = invocation.getArgument(0);
            MultipartUploadPart part = new MultipartUploadPart();
            part.setId(EntityId.of(9401L));
            part.setUploadId(command.getUploadId());
            part.setPartNumber(command.getPartNumber());
            part.setSize(command.getSize());
            return part;
        });
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setParameter("partNumber", "2");
        request.addFile(new MockMultipartFile("file", "part-2", "application/octet-stream", new byte[] {1, 2, 3}));
        MultipartUploadController controller = controller(service, mock(StoredObjectStore.class));

        MultipartUploadPartResponse response = controller.uploadPart("upload-1", request);

        ArgumentCaptor<UploadMultipartPartCommand> captor =
                ArgumentCaptor.forClass(UploadMultipartPartCommand.class);
        verify(service).uploadPart(captor.capture());
        assertEquals(Long.valueOf(9401L), response.getId());
        assertEquals("upload-1", captor.getValue().getUploadId());
        assertEquals(Integer.valueOf(2), captor.getValue().getPartNumber());
        assertEquals(Long.valueOf(3L), captor.getValue().getSize());
    }

    @Test(expected = com.github.thundax.common.exception.InvalidParameterException.class)
    public void shouldRejectNonMultipartPartUpload() throws Exception {
        MultipartUploadController controller =
                controller(mock(MultipartUploadService.class), mock(StoredObjectStore.class));

        controller.uploadPart("upload-1", new MockHttpServletRequest());
    }

    @Test
    public void shouldCompleteMultipartUploadWithStorageMetadata() {
        MultipartUploadService service = mock(MultipartUploadService.class);
        StoredObjectStore store = mock(StoredObjectStore.class);
        when(store.type()).thenReturn(StorageType.LOCAL_FILE);
        StoredObject storage = new StoredObject();
        storage.setId(EntityId.of(9101L));
        storage.setOriginalFilename("demo.png");
        when(service.complete(any(CompleteMultipartUploadCommand.class))).thenReturn(storage);
        MultipartUploadController controller = controller(service, store);
        MultipartUploadCompleteRequest request = new MultipartUploadCompleteRequest();
        request.setObjectKey("202605/demo.png");
        request.setSize(300L);

        controller.complete("upload-1", request);

        ArgumentCaptor<CompleteMultipartUploadCommand> captor =
                ArgumentCaptor.forClass(CompleteMultipartUploadCommand.class);
        verify(service).complete(captor.capture());
        assertEquals("upload-1", captor.getValue().getUploadId());
        assertEquals(StorageType.LOCAL_FILE, captor.getValue().getStorageType());
        assertEquals("202605/demo.png", captor.getValue().getObjectKey());
        assertEquals(Long.valueOf(300L), captor.getValue().getSize());
    }

    @Test
    public void shouldAbortMultipartUpload() {
        MultipartUploadService service = mock(MultipartUploadService.class);
        when(service.abort(any(AbortMultipartUploadCommand.class))).thenReturn(1);
        MultipartUploadController controller = controller(service, mock(StoredObjectStore.class));

        assertEquals(Boolean.TRUE, controller.abort("upload-1"));
    }

    private MultipartUploadController controller(MultipartUploadService service, StoredObjectStore store) {
        return new MultipartUploadController(service, store, mock(StorageConverter.class));
    }
}
