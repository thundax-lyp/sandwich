package com.github.thundax.modules.submission.service.command;

import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubmissionCommand {
    private String title;
    private String content;
    private List<StoredObjectId> imageObjectIds;
}
