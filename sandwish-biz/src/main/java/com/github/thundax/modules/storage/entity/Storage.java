package com.github.thundax.modules.storage.entity;

import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.storage.utils.MetaFile;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Storage implements Sortable {
    private EntityId id;
    private String name;
    private String extendName;
    private String mimeType;
    private String ownerId;
    private StorageOwnerType ownerType;
    private StorageStatus status = StorageStatus.ENABLED;
    private StorageVisibility visibility = StorageVisibility.PRIVATE;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }

    public static final String BUSINESS_TYPE_UNDEFINED = "undefined";

    private static final String PATH_FORMAT = "yyyyMM";

    public void setStatus(String status) {
        this.status = StringUtils.isBlank(status) ? null : StorageStatus.from(status);
    }

    public void setStatus(StorageStatus status) {
        this.status = status;
    }

    public void setVisibility(String visibility) {
        this.visibility = StringUtils.isBlank(visibility) ? null : StorageVisibility.from(visibility);
    }

    public void setVisibility(StorageVisibility visibility) {
        this.visibility = visibility;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = StringUtils.isBlank(ownerType) ? null : StorageOwnerType.from(ownerType);
    }

    public void setOwnerType(StorageOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public boolean isEnable() {
        return StorageStatus.ENABLED == getStatus();
    }

    public String getFileName() {
        return EntityIdCodec.toValue(getId()) + MetaFile.DOT + this.getExtendName();
    }

    public String getOriginalFileName() {
        return this.getName() + MetaFile.DOT + this.getExtendName();
    }

    public String getPathName() {
        return new SimpleDateFormat(PATH_FORMAT).format(this.getCreateDate()) + MetaFile.SEPARATOR + this.getFileName();
    }

    private Query query;

    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

        public static final String PROP_MIME_TYPE = "mimeType";
        public static final String PROP_BUSINESS_ID = "businessId";
        public static final String PROP_BUSINESS_TYPE = "businessType";
        public static final String PROP_OWNER_ID = "ownerId";
        public static final String PROP_OWNER_TYPE = "ownerType";
        public static final String PROP_STATUS = "status";
        public static final String PROP_VISIBILITY = "visibility";
        public static final String PROP_NAME = "name";
        public static final String PROP_REMARKS = "remarks";

        private String mimeType;
        private String businessId;
        private String businessType;
        private String ownerId;
        private StorageOwnerType ownerType;
        private StorageStatus status;
        private StorageVisibility visibility;
        private String name;
        private String remarks;

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public String getBusinessType() {
            return businessType;
        }

        public void setBusinessType(String businessType) {
            this.businessType = businessType;
        }

        public String getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(String ownerId) {
            this.ownerId = ownerId;
        }

        public StorageOwnerType getOwnerType() {
            return ownerType;
        }

        public void setOwnerType(String ownerType) {
            this.ownerType = StringUtils.isBlank(ownerType) ? null : StorageOwnerType.from(ownerType);
        }

        public void setOwnerType(StorageOwnerType ownerType) {
            this.ownerType = ownerType;
        }

        public StorageStatus getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = StringUtils.isBlank(status) ? null : StorageStatus.from(status);
        }

        public void setStatus(StorageStatus status) {
            this.status = status;
        }

        public StorageVisibility getVisibility() {
            return visibility;
        }

        public void setVisibility(String visibility) {
            this.visibility = StringUtils.isBlank(visibility) ? null : StorageVisibility.from(visibility);
        }

        public void setVisibility(StorageVisibility visibility) {
            this.visibility = visibility;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}
