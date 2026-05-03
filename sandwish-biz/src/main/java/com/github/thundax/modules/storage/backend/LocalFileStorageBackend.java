package com.github.thundax.modules.storage.backend;

import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.enums.StorageBackendType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LocalFileStorageBackend implements StorageBackend {

    private static final int BUFFER_SIZE = 4096;

    private final String storagePath;
    private final String servletPath;

    public LocalFileStorageBackend(String storagePath, String servletPath) {
        this.storagePath = storagePath;
        this.servletPath = servletPath;
    }

    @Override
    public StorageBackendType type() {
        return StorageBackendType.LOCAL_FILE;
    }

    @Override
    public StorageBackendObject save(Storage storage, InputStream inputStream) throws IOException {
        File file = toFile(storage);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (InputStream source = inputStream;
                FileOutputStream target = new FileOutputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int readBytes;
            while ((readBytes = source.read(buffer)) > -1) {
                target.write(buffer, 0, readBytes);
            }
            target.flush();
        }

        StorageBackendObject object = new StorageBackendObject();
        object.setStorageType(type());
        object.setBucketName(storagePath);
        object.setObjectKey(storage.getPathName());
        object.setSize(file.length());
        object.setAccessEndpoint(servletPath + storage.getFileName());
        return object;
    }

    @Override
    public boolean exists(Storage storage) {
        return toFile(storage).exists();
    }

    @Override
    public InputStream open(Storage storage) throws IOException {
        return new FileInputStream(toFile(storage));
    }

    private File toFile(Storage storage) {
        String objectKey = storage.getObjectKey() == null ? storage.getPathName() : storage.getObjectKey();
        return new File(storagePath + objectKey);
    }
}
