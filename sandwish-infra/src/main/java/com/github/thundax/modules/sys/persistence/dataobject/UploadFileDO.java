package com.github.thundax.modules.sys.persistence.dataobject;

import com.github.thundax.common.persistence.AdminDataEntity;

/**
 * 上传文件持久化对象。
 */
public class UploadFileDO extends AdminDataEntity<UploadFileDO> {

    private static final long serialVersionUID = 1L;

    private String name;
    private String extendName;
    private String mimeType;
    private Long size;
    private String path;
    private byte[] content;

    public UploadFileDO() {
        super();
    }

    public UploadFileDO(String id) {
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
