package com.github.thundax.modules.storage.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import org.junit.Test;

public class LocalFileStoredObjectStoreTest {

    @Test
    public void shouldSaveAndOpenLocalStorageObject() throws Exception {
        Path directory = Files.createTempDirectory("sandwish-storage-");
        StoredObject storage = new StoredObject();
        storage.setId(EntityIdCodec.toDomain("s1"));
        storage.setExtendName("txt");
        storage.setCreateDate(new Date());
        LocalFileStoredObjectStore backend =
                new LocalFileStoredObjectStore(directory.toString() + "/", "/api/assist/storage/objects/");

        StoredObject object = backend.save(storage, new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

        storage.setObjectKey(object.getObjectKey());

        assertEquals(StorageType.LOCAL_FILE, object.getStorageType());
        assertEquals(directory.toString() + "/", object.getBucketName());
        assertEquals(Long.valueOf(5L), object.getSize());
        assertEquals("/api/assist/storage/objects/s1/content", object.getAccessEndpoint());
        assertTrue(backend.exists(storage));
        try (InputStream inputStream = backend.open(storage)) {
            assertEquals(5, inputStream.available());
        }
    }
}
