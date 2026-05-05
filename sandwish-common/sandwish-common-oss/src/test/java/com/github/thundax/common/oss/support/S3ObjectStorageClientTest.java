package com.github.thundax.common.oss.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.github.thundax.common.oss.model.ObjectStorageWriteResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class S3ObjectStorageClientTest {

    @Test
    public void shouldWriteS3Object() throws Exception {
        AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
        S3ObjectStorageClient client = new S3ObjectStorageClient(amazonS3, "bucket-a", "s3://bucket-a/");

        ObjectStorageWriteResult result =
                client.put("avatars/user.txt", new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

        ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
        Mockito.verify(amazonS3)
                .putObject(
                        Mockito.eq("bucket-a"),
                        Mockito.eq("avatars/user.txt"),
                        Mockito.any(ByteArrayInputStream.class),
                        metadataCaptor.capture());
        assertEquals(5L, metadataCaptor.getValue().getContentLength());
        assertEquals("avatars/user.txt", result.getKey());
        assertEquals("s3://bucket-a/avatars/user.txt", result.getLocation());
        assertEquals(5L, result.getSize());
    }

    @Test
    public void shouldDelegateExistsAndDelete() {
        AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
        Mockito.when(amazonS3.doesObjectExist("bucket-a", "a.txt")).thenReturn(true);
        S3ObjectStorageClient client = new S3ObjectStorageClient(amazonS3, "bucket-a", "s3://bucket-a/");

        assertTrue(client.exists("a.txt"));
        client.delete("a.txt");

        Mockito.verify(amazonS3).deleteObject("bucket-a", "a.txt");
    }
}
