package com.github.thundax.modules.storage.backend;

import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.enums.StorageBackendType;
import java.io.IOException;
import java.io.InputStream;

public interface StorageBackend {

    StorageBackendType type();

    StorageBackendObject save(Storage storage, InputStream inputStream) throws IOException;

    boolean exists(Storage storage);

    InputStream open(Storage storage) throws IOException;
}
