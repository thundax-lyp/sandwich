package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.command.CreateStorageCommand;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.command.ChangeCurrentUserAvatarCommand;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import javax.imageio.ImageIO;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class CurrentUserAvatarServiceTest {

    @Test
    public void shouldStoreAvatarAsUserOwnedStorageObject() throws Exception {
        StorageService storageService = mock(StorageService.class);
        StoredObjectStore storedObjectStore = mock(StoredObjectStore.class);
        StoredObject oldAvatar = new StoredObject();
        oldAvatar.setId(StoredObjectIdCodec.toDomain(7001L));

        when(storageService.list(any())).thenReturn(Collections.singletonList(oldAvatar));
        when(storageService.create(any())).thenReturn(StoredObjectIdCodec.toDomain(8001L));
        when(storedObjectStore.save(any(), any(InputStream.class))).thenAnswer(invocation -> {
            StoredObject storedObject = new StoredObject();
            storedObject.setBucketName("local");
            storedObject.setObjectKey("202605/avatar.jpg");
            storedObject.setSize(12L);
            return storedObject;
        });

        CurrentUserServiceImpl service = new CurrentUserServiceImpl(
                mock(UserService.class),
                mock(RoleService.class),
                mock(MenuService.class),
                mock(PrincipalIdentityService.class),
                mock(PrincipalCredentialService.class),
                storageService,
                storedObjectStore);
        MultipartFile avatar = avatarFile();

        service.changeAvatar(new ChangeCurrentUserAvatarCommand(
                UserIdCodec.toDomain(7L), avatar.getInputStream(), avatar.getOriginalFilename()));

        ArgumentCaptor<StoredObjectId> deleteCaptor = ArgumentCaptor.forClass(StoredObjectId.class);
        verify(storageService).remove(deleteCaptor.capture());
        assertEquals(StoredObjectIdCodec.toDomain(7001L), deleteCaptor.getValue());

        ArgumentCaptor<CreateStorageCommand> createCaptor = ArgumentCaptor.forClass(CreateStorageCommand.class);
        verify(storageService).create(createCaptor.capture());
        assertEquals("7", createCaptor.getValue().getOwnerId());
        assertEquals(StorageOwnerType.USER, createCaptor.getValue().getOwnerType());
        assertEquals(StoredObjectStatus.ACTIVE, createCaptor.getValue().getObjectStatus());
        assertEquals(
                StoredObjectReferenceStatus.UNREFERENCED,
                createCaptor.getValue().getReferenceStatus());
        assertTrue(createCaptor.getValue().getSize() > 0L);
        assertNotNull(createCaptor.getValue().getObjectKey());
        assertEquals("local", createCaptor.getValue().getBucketName());
        assertEquals("avatar", createCaptor.getValue().getRemarks());
    }

    private MultipartFile avatarFile() throws Exception {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        return new MockMultipartFile("avatar", "avatar.png", "image/png", outputStream.toByteArray());
    }
}
