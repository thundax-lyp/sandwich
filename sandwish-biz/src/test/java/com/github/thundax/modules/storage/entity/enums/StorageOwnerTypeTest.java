package com.github.thundax.modules.storage.entity.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StorageOwnerTypeTest {

    @Test
    public void shouldParseSubmissionOwnerType() {
        assertEquals(StorageOwnerType.SUBMISSION, StorageOwnerType.from("SUBMISSION"));
        assertEquals("SUBMISSION", StorageOwnerType.SUBMISSION.value());
    }
}
