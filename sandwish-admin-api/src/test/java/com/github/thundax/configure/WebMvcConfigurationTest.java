package com.github.thundax.configure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.oss.configure.SandwishOssProperties;
import com.github.thundax.common.oss.support.LocalFileObjectStorageClient;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.store.StoredObjectStore;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WebMvcConfigurationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldUseOssLocalRootPathForLocalStoredObjects() throws Exception {
        String rootPath = temporaryFolder.newFolder("object").getAbsolutePath();
        SandwishOssProperties ossProperties = new SandwishOssProperties();
        ossProperties.getLocal().setRootPath(rootPath);
        ossProperties.getLocal().setLocationPrefix("file:");
        StoredObjectStore store = new WebMvcConfiguration()
                .storedObjectStore(
                        new SandwishProperties(), new LocalFileObjectStorageClient(rootPath, "file:"), ossProperties);
        StoredObject object = new StoredObject();
        object.setExtendName("txt");

        StoredObject stored = store.save(object, new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

        assertEquals(rootPath, stored.getBucketName());
        assertTrue(stored.getObjectKey().matches("\\d{6}/.+\\.txt"));
        Path target = new java.io.File(rootPath, stored.getObjectKey()).toPath();
        assertTrue(Files.exists(target));
    }
}
