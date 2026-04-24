package com.github.thundax.modules.storage.entity.base;

import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.modules.storage.entity.Storage;

/**
 * @author wdit
 */
public abstract class BaseStorage extends DataEntity<Storage> {

    private static final long serialVersionUID = 1L;

    public BaseStorage() {
    }

    public BaseStorage(String id) {
        super(id);
    }

    private String name;
    private String extendName;
    private String mimeType;
    private String ownerId;
    private String ownerType;
    private String enableFlag;

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


    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag) {
        this.enableFlag = enableFlag;
    }

}
