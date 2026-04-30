package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseUploadFile {
    private EntityId id;

    private String name;
    private String extendName;
    private String mimeType;
    private Long size;
    private String path;
    private byte[] content;
    private Date createDate;

    public BaseUploadFile() {}
}
