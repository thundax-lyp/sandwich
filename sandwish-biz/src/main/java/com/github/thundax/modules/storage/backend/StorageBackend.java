package com.github.thundax.modules.storage.backend;

import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import java.io.IOException;
import java.io.InputStream;

public interface StorageBackend {

    StorageType type();

    StorageBackendObject save(StoredObject storage, InputStream inputStream) throws IOException;

    boolean exists(StoredObject storage);

    InputStream open(StoredObject storage) throws IOException;
}
