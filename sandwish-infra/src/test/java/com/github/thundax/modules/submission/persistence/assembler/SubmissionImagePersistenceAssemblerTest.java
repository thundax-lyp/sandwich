package com.github.thundax.modules.submission.persistence.assembler;

import static org.junit.Assert.assertEquals;

import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.submission.entity.SubmissionImage;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionImageId;
import com.github.thundax.modules.submission.persistence.dataobject.SubmissionImageDO;
import org.junit.Test;

public class SubmissionImagePersistenceAssemblerTest {

    @Test
    public void shouldMapImageReferenceFields() {
        SubmissionImage entity = new SubmissionImage();
        entity.setId(SubmissionImageId.of(7001L));
        entity.setSubmissionId(SubmissionId.of(9001L));
        entity.setStorageObjectId(StoredObjectId.of(8001L));
        entity.setSortOrder(2);

        SubmissionImageDO dataObject = SubmissionImagePersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(7001L), dataObject.getId());
        assertEquals(Long.valueOf(9001L), dataObject.getSubmissionId());
        assertEquals(Long.valueOf(8001L), dataObject.getStorageObjectId());
        assertEquals(Integer.valueOf(2), dataObject.getSortOrder());
        assertEquals(
                entity.getStorageObjectId(),
                SubmissionImagePersistenceAssembler.toEntity(dataObject).getStorageObjectId());
    }
}
