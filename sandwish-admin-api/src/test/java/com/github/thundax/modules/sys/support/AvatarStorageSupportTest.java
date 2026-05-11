package com.github.thundax.modules.sys.support;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.command.ChangeStorageCommand;
import com.github.thundax.modules.storage.service.command.CreateStorageCommand;
import com.github.thundax.modules.storage.service.command.DeleteStorageCommand;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import javax.imageio.ImageIO;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class AvatarStorageSupportTest {

    @Test
    public void shouldStoreAvatarAsUserOwnedStorageObject() throws Exception {
        StorageService storageService = mock(StorageService.class);
        StoredObjectStore storedObjectStore = mock(StoredObjectStore.class);
        StoredObject oldAvatar = new StoredObject();
        oldAvatar.setId(StoredObjectIdCodec.toDomain(7001L));

        when(storageService.list(any())).thenReturn(Collections.singletonList(oldAvatar));
        when(storageService.create(any())).thenReturn(StoredObjectIdCodec.toDomain(8001L));
        when(storedObjectStore.save(any(), any(InputStream.class))).thenAnswer(invocation -> {
            StoredObject storage = invocation.getArgument(0);
            StoredObject storedObject = new StoredObject();
            storedObject.setStorageType(StorageType.LOCAL_FILE);
            storedObject.setBucketName("local");
            storedObject.setObjectKey(storage.getPathName());
            storedObject.setSize(12L);
            return storedObject;
        });

        AvatarStorageSupport support = new AvatarStorageSupport(storageService, storedObjectStore);

        support.saveAvatar(UserIdCodec.toDomain(7L), avatarFile());

        ArgumentCaptor<DeleteStorageCommand> deleteCaptor = ArgumentCaptor.forClass(DeleteStorageCommand.class);
        verify(storageService).remove(deleteCaptor.capture());
        assertEquals(
                StoredObjectIdCodec.toDomain(7001L), deleteCaptor.getValue().getId());

        ArgumentCaptor<CreateStorageCommand> createCaptor = ArgumentCaptor.forClass(CreateStorageCommand.class);
        verify(storageService).create(createCaptor.capture());
        assertEquals("7", createCaptor.getValue().getOwnerId());
        assertEquals(StorageOwnerType.USER, createCaptor.getValue().getOwnerType());
        assertEquals(StoredObjectStatus.ACTIVE, createCaptor.getValue().getObjectStatus());
        assertEquals(
                StoredObjectReferenceStatus.UNREFERENCED,
                createCaptor.getValue().getReferenceStatus());
        assertEquals("avatar", createCaptor.getValue().getRemarks());

        ArgumentCaptor<ChangeStorageCommand> changeCaptor = ArgumentCaptor.forClass(ChangeStorageCommand.class);
        verify(storageService).change(changeCaptor.capture());
        assertEquals(
                StoredObjectIdCodec.toDomain(8001L), changeCaptor.getValue().getId());
        assertEquals(StorageType.LOCAL_FILE, changeCaptor.getValue().getStorageType());
        assertEquals("local", changeCaptor.getValue().getBucketName());
        assertEquals(Long.valueOf(12L), changeCaptor.getValue().getSize());
    }

    private MultipartFile avatarFile() throws Exception {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        return new MockMultipartFile("avatar", "avatar.png", "image/png", outputStream.toByteArray());
    }
}
