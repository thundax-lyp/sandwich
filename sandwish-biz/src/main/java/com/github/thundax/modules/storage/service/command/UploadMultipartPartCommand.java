package com.github.thundax.modules.storage.service.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadMultipartPartCommand {
    private String uploadId;
    private Integer partNumber;
    private String etag;
    private Long size;
}
