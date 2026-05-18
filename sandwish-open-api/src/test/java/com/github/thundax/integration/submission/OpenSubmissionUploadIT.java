package com.github.thundax.integration.submission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.thundax.integration.AbstractOpenApiIT;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

public class OpenSubmissionUploadIT extends AbstractOpenApiIT {

    private static final String UPLOAD_PATH = "/api/submission/submission/image/upload";

    @Before
    public void prepareData() {
        prepareIntegrationData();
    }

    @Test
    public void shouldUploadSubmissionImage() {
        byte[] image = new byte[] {(byte) 0xff, (byte) 0xd8, 1, 2, 3, (byte) 0xff, (byte) 0xd9};

        Map<String, Object> uploaded = dataMap(postSignedMultipart(
                UPLOAD_PATH, "integration-open-submission.jpg", image, MediaType.IMAGE_JPEG, null, Map.class));

        assertNotNull(uploaded.get("id"));
        assertEquals("integration-open-submission.jpg", uploaded.get("originalFilename"));
        assertEquals("image/jpeg", uploaded.get("contentType"));
    }
}
