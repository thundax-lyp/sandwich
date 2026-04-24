package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.persistence.AdminDataEntity;
import com.github.thundax.modules.sys.entity.UploadFile;

public class BaseUploadFile extends AdminDataEntity<UploadFile> {
    private String name;
    private String extendName;
    private String mimeType;
    private Long size;
    private String path;
    private byte[] content;

    public BaseUploadFile() {
    }

    public BaseUploadFile(String id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtendName() {
        return extendName;
    }

    public void setExtendName(String extendName) {
        this.extendName = extendName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
