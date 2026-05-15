package com.github.thundax.modules.submission.entity;

import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionImageId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionImage {
    private SubmissionImageId id;
    private SubmissionId submissionId;
    private StoredObjectId storageObjectId;
    private int sortOrder;
}
