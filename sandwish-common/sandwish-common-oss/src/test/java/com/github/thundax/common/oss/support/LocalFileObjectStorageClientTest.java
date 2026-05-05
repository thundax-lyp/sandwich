package com.github.thundax.common.oss.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.oss.model.ObjectStorageWriteResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LocalFileObjectStorageClientTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldWriteReadAndDeleteLocalObject() throws Exception {
        LocalFileObjectStorageClient client =
                new LocalFileObjectStorageClient(temporaryFolder.getRoot().getAbsolutePath(), "file:");

        ObjectStorageWriteResult result =
                client.put("avatars/user.txt", new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

        assertEquals("avatars/user.txt", result.getKey());
        assertEquals("file:avatars/user.txt", result.getLocation());
        assertEquals(5L, result.getSize());
        assertTrue(client.exists("avatars/user.txt"));
        assertEquals("hello", IOUtils.toString(client.get("avatars/user.txt"), StandardCharsets.UTF_8));

        client.delete("avatars/user.txt");

        assertFalse(client.exists("avatars/user.txt"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectEscapedKey() throws Exception {
        LocalFileObjectStorageClient client =
                new LocalFileObjectStorageClient(temporaryFolder.getRoot().getAbsolutePath(), "file:");

        client.put("../outside.txt", new ByteArrayInputStream("bad".getBytes(StandardCharsets.UTF_8)));
    }
}
