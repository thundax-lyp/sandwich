package com.github.thundax.modules.storage.store;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.oss.client.ObjectStorageClient;
import com.github.thundax.common.oss.model.ObjectStorageWriteResult;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import java.io.IOException;
import java.io.InputStream;

public class ObjectStorageStoredObjectStore implements StoredObjectStore {

    private final ObjectStorageClient objectStorageClient;
    private final StorageType storageType;
    private final String bucketName;
    private final String contentPath;

    public ObjectStorageStoredObjectStore(
            ObjectStorageClient objectStorageClient, StorageType storageType, String bucketName, String contentPath) {
        this.objectStorageClient = objectStorageClient;
        this.storageType = storageType;
        this.bucketName = bucketName;
        this.contentPath = contentPath;
    }

    @Override
    public StorageType type() {
        return storageType;
    }

    @Override
    public StoredObject save(StoredObject storage, InputStream inputStream) throws IOException {
        ObjectStorageWriteResult result = objectStorageClient.put(storage.getPathName(), inputStream);
        StoredObject storedObject = new StoredObject();
        storedObject.setStorageType(type());
        storedObject.setBucketName(bucketName);
        storedObject.setObjectKey(result.getKey());
        storedObject.setSize(result.getSize());
        storedObject.setAccessEndpoint(contentPath + EntityIdCodec.toValue(storage.getId()) + "/content");
        return storedObject;
    }

    @Override
    public boolean exists(StoredObject storage) {
        return objectStorageClient.exists(objectKey(storage));
    }

    @Override
    public InputStream open(StoredObject storage) throws IOException {
        return objectStorageClient.get(objectKey(storage));
    }

    private String objectKey(StoredObject storage) {
        return storage.getObjectKey() == null ? storage.getPathName() : storage.getObjectKey();
    }
}
