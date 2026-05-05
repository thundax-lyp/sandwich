package com.github.thundax.modules.storage.store;

import com.github.thundax.common.oss.support.LocalFileObjectStorageClient;
import com.github.thundax.modules.storage.entity.enums.StorageType;

public class LocalFileStoredObjectStore extends ObjectStorageStoredObjectStore {

    public LocalFileStoredObjectStore(String storagePath, String contentPath) {
        super(new LocalFileObjectStorageClient(storagePath, ""), StorageType.LOCAL_FILE, storagePath, contentPath);
    }
}
