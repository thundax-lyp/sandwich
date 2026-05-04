package com.github.thundax.modules.storage.store;

import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LocalFileStoredObjectStore implements StoredObjectStore {

    private static final int BUFFER_SIZE = 4096;

    private final String storagePath;
    private final String servletPath;

    public LocalFileStoredObjectStore(String storagePath, String servletPath) {
        this.storagePath = storagePath;
        this.servletPath = servletPath;
    }

    @Override
    public StorageType type() {
        return StorageType.LOCAL_FILE;
    }

    @Override
    public StoredObject save(StoredObject storage, InputStream inputStream) throws IOException {
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

        StoredObject storedObject = new StoredObject();
        storedObject.setStorageType(type());
        storedObject.setBucketName(storagePath);
        storedObject.setObjectKey(storage.getPathName());
        storedObject.setSize(file.length());
        storedObject.setAccessEndpoint(servletPath + storage.getFileName());
        return storedObject;
    }

    @Override
    public boolean exists(StoredObject storage) {
        return toFile(storage).exists();
    }

    @Override
    public InputStream open(StoredObject storage) throws IOException {
        return new FileInputStream(toFile(storage));
    }

    private File toFile(StoredObject storage) {
        String objectKey = storage.getObjectKey() == null ? storage.getPathName() : storage.getObjectKey();
        return new File(storagePath + objectKey);
    }
}
