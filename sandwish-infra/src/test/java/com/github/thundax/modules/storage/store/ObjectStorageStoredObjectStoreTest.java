package com.github.thundax.modules.storage.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.oss.client.ObjectStorageClient;
import com.github.thundax.common.oss.model.ObjectStorageWriteResult;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.Test;

public class ObjectStorageStoredObjectStoreTest {

    @Test
    public void shouldSaveThroughObjectStorageClient() throws Exception {
        RecordingObjectStorageClient client = new RecordingObjectStorageClient();
        ObjectStorageStoredObjectStore store =
                new ObjectStorageStoredObjectStore(client, StorageType.OSS, "bucket-a", "/content/");
        StoredObject storage = storage();

        StoredObject object = store.save(storage, new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

        assertEquals(StorageType.OSS, object.getStorageType());
        assertEquals("bucket-a", object.getBucketName());
        assertEquals(storage.getPathName(), object.getObjectKey());
        assertEquals(Long.valueOf(5L), object.getSize());
        assertEquals("/content/s1/content", object.getAccessEndpoint());
        assertTrue(client.exists(object.getObjectKey()));
    }

    private StoredObject storage() {
        StoredObject storage = new StoredObject();
        storage.setId(EntityIdCodec.toDomain("s1"));
        storage.setExtendName("txt");
        storage.setCreateDate(new Date());
        return storage;
    }

    private static class RecordingObjectStorageClient implements ObjectStorageClient {

        private String key;
        private byte[] data;

        @Override
        public ObjectStorageWriteResult put(String key, InputStream inputStream) throws IOException {
            this.key = key;
            this.data = read(inputStream);
            ObjectStorageWriteResult result = new ObjectStorageWriteResult();
            result.setKey(key);
            result.setLocation(key);
            result.setSize(data.length);
            return result;
        }

        @Override
        public InputStream get(String key) {
            return new ByteArrayInputStream(data);
        }

        @Override
        public boolean exists(String key) {
            return key.equals(this.key);
        }

        @Override
        public void delete(String key) {}

        private byte[] read(InputStream inputStream) throws IOException {
            byte[] buffer = new byte[8192];
            int length = inputStream.read(buffer);
            byte[] bytes = new byte[length];
            System.arraycopy(buffer, 0, bytes, 0, length);
            return bytes;
        }
    }
}
