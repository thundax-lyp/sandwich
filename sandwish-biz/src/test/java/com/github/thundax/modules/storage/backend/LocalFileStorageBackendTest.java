package com.github.thundax.modules.storage.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.enums.StorageBackendType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import org.junit.Test;

public class LocalFileStorageBackendTest {

    @Test
    public void shouldSaveAndOpenLocalStorageObject() throws Exception {
        Path directory = Files.createTempDirectory("sandwish-storage-");
        Storage storage = new Storage();
        storage.setId(EntityIdCodec.toDomain("s1"));
        storage.setExtendName("txt");
        storage.setCreateDate(new Date());
        LocalFileStorageBackend backend = new LocalFileStorageBackend(directory.toString() + "/", "/servlet/storage/");

        StorageBackendObject object =
                backend.save(storage, new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

        storage.setObjectKey(object.getObjectKey());

        assertEquals(StorageBackendType.LOCAL_FILE, object.getStorageType());
        assertEquals(directory.toString() + "/", object.getBucketName());
        assertEquals(Long.valueOf(5L), object.getSize());
        assertEquals("/servlet/storage/s1.txt", object.getAccessEndpoint());
        assertTrue(backend.exists(storage));
        try (InputStream inputStream = backend.open(storage)) {
            assertEquals(5, inputStream.available());
        }
    }
}
