package com.github.thundax.modules.sys.support;

import com.github.thundax.common.exception.AdminResponseExceptions;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.entity.enums.StoredObjectReferenceStatus;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.service.StorageService;
import com.github.thundax.modules.storage.service.command.ChangeStorageCommand;
import com.github.thundax.modules.storage.service.command.CreateStorageCommand;
import com.github.thundax.modules.storage.service.command.DeleteStorageCommand;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AvatarStorageSupport {

    private static final String AVATAR_REMARKS = "avatar";
    private static final String AVATAR_NAME = "avatar";
    private static final String AVATAR_FILENAME = "avatar.jpg";
    private static final String JPG = "jpg";
    private static final int MAX_AVATAR_WIDTH = 400;
    private static final int MAX_AVATAR_HEIGHT = 400;
    private static final float IMAGE_QUALITY = 0.8f;

    private final StorageService storageService;
    private final StoredObjectStore storedObjectStore;

    public AvatarStorageSupport(StorageService storageService, StoredObjectStore storedObjectStore) {
        this.storageService = storageService;
        this.storedObjectStore = storedObjectStore;
    }

    public StoredObject saveAvatar(UserId userId, MultipartFile avatar) {
        if (userId == null || avatar == null || avatar.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("avatar");
        }

        removeAvatar(userId);

        byte[] avatarBytes = readAvatarBytes(avatar);
        StoredObject storage = toAvatarStorage(userId, avatar);
        storage.setId(storageService.create(toCreateStorageCommand(storage)));
        try {
            applyStoredObject(storage, storedObjectStore.save(storage, new ByteArrayInputStream(avatarBytes)));
        } catch (IOException e) {
            throw AdminResponseExceptions.system(e.getMessage());
        }
        storageService.change(toChangeStorageCommand(storage));
        return storage;
    }

    public void removeAvatar(UserId userId) {
        for (StoredObject storage : listAvatars(userId)) {
            storageService.remove(new DeleteStorageCommand(storage.getId()));
        }
    }

    public StoredObject getAvatar(UserId userId) {
        List<StoredObject> avatars = listAvatars(userId);
        return avatars.isEmpty() ? null : avatars.get(avatars.size() - 1);
    }

    public boolean existsAvatar(UserId userId) {
        StoredObject avatar = getAvatar(userId);
        return avatar != null && storedObjectStore.exists(avatar);
    }

    public void writeAvatar(UserId userId, HttpServletResponse response) throws IOException {
        StoredObject avatar = getAvatar(userId);
        if (avatar == null || !storedObjectStore.exists(avatar)) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType(avatar.getMimeType());

        try (InputStream inputStream = storedObjectStore.open(avatar);
                OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, readBytes);
            }
        }
    }

    private List<StoredObject> listAvatars(UserId userId) {
        StorageQuery query = new StorageQuery();
        query.setOwnerType(StorageOwnerType.USER);
        query.setOwnerId(UserIdCodec.toStringValue(userId));
        query.setObjectStatus(StoredObjectStatus.ACTIVE);
        query.setRemarks(AVATAR_REMARKS);
        return storageService.list(query);
    }

    private byte[] readAvatarBytes(MultipartFile avatar) {
        try (InputStream inputStream = avatar.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.Builder<?> builder = Thumbnails.of(inputStream);
            BufferedImage image = builder.scale(1.0f).asBufferedImage();

            int originWidth = image.getWidth();
            int originHeight = image.getHeight();
            if (originWidth <= 0 || originHeight <= 0) {
                throw AdminResponseExceptions.invalidParameter("avatar");
            }

            if (originWidth > MAX_AVATAR_WIDTH || originHeight > MAX_AVATAR_HEIGHT) {
                double scale = Math.min(
                        (double) MAX_AVATAR_WIDTH / (double) originWidth,
                        (double) MAX_AVATAR_HEIGHT / (double) originHeight);
                builder = Thumbnails.of(image).scale(scale);
            } else {
                builder = Thumbnails.of(image).size(originWidth, originHeight);
            }

            builder.outputFormat(JPG);
            builder.outputQuality(IMAGE_QUALITY);
            builder.toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw AdminResponseExceptions.system(e.getMessage());
        } catch (RuntimeException e) {
            throw AdminResponseExceptions.invalidParameter("avatar");
        }
    }

    private StoredObject toAvatarStorage(UserId userId, MultipartFile avatar) {
        StoredObject storage = new StoredObject();
        storage.setOriginalFilename(originalFilename(avatar));
        storage.setContentType(MediaType.IMAGE_JPEG_VALUE);
        storage.setName(AVATAR_NAME);
        storage.setExtendName(JPG);
        storage.setMimeType(MediaType.IMAGE_JPEG_VALUE);
        storage.setOwnerId(UserIdCodec.toStringValue(userId));
        storage.setOwnerType(StorageOwnerType.USER);
        storage.setObjectStatus(StoredObjectStatus.ACTIVE);
        storage.setReferenceStatus(StoredObjectReferenceStatus.UNREFERENCED);
        storage.setRemarks(AVATAR_REMARKS);
        return storage;
    }

    private String originalFilename(MultipartFile avatar) {
        String originalFilename = avatar.getOriginalFilename();
        if (StringUtils.isBlank(FilenameUtils.getExtension(originalFilename))) {
            return AVATAR_FILENAME;
        }
        return originalFilename;
    }

    private void applyStoredObject(StoredObject storage, StoredObject storedObject) {
        storage.setStorageType(storedObject.getStorageType());
        storage.setBucketName(storedObject.getBucketName());
        storage.setObjectKey(storedObject.getObjectKey());
        storage.setSize(storedObject.getSize());
        storage.setAccessEndpoint(storedObject.getAccessEndpoint());
    }

    private CreateStorageCommand toCreateStorageCommand(StoredObject storage) {
        CreateStorageCommand command = new CreateStorageCommand();
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeType());
        command.setOwnerId(storage.getOwnerId());
        command.setOwnerType(storage.getOwnerType());
        command.setObjectStatus(storage.getObjectStatus());
        command.setReferenceStatus(storage.getReferenceStatus());
        command.setRemarks(storage.getRemarks());
        return command;
    }

    private ChangeStorageCommand toChangeStorageCommand(StoredObject storage) {
        ChangeStorageCommand command = new ChangeStorageCommand();
        command.setId(storage.getId());
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeType());
        command.setOwnerId(storage.getOwnerId());
        command.setOwnerType(storage.getOwnerType());
        command.setStorageType(storage.getStorageType());
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
