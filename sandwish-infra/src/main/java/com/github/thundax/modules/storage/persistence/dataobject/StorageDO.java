package com.github.thundax.modules.storage.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 存储文件持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("assist_storage")
public class StorageDO {

    public static final String DEL_FLAG_NORMAL = "0";

    private String id;
    private boolean isNewRecord;

    private String name;
    private String extendName;
    private String mimeType;
    private String businessId;
    private String businessType;
    private String businessParams;
    private String ownerId;
    private String ownerType;
    private String enableFlag;
    private String publicFlag;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String delFlag;
    private String queryMimeType;
    private String queryBusinessId;
    private String queryBusinessType;
    private String queryOwnerId;
    private String queryOwnerType;
    private String queryEnableFlag;
    private String queryPublicFlag;
    private String queryName;
    private String queryRemarks;

    public StorageDO(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getIsNewRecord() {
        return isNewRecord;
    }

    public void setIsNewRecord(boolean isNewRecord) {
        this.isNewRecord = isNewRecord;
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

    public String getBusinessParams() {
        return businessParams;
    }

    public void setBusinessParams(String businessParams) {
        this.businessParams = businessParams;
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

    public String getPublicFlag() {
        return publicFlag;
    }

    public void setPublicFlag(String publicFlag) {
        this.publicFlag = publicFlag;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getQueryMimeType() {
        return queryMimeType;
    }

    public void setQueryMimeType(String queryMimeType) {
        this.queryMimeType = queryMimeType;
    }

    public String getQueryBusinessId() {
        return queryBusinessId;
    }

    public void setQueryBusinessId(String queryBusinessId) {
        this.queryBusinessId = queryBusinessId;
    }

    public String getQueryBusinessType() {
        return queryBusinessType;
    }

    public void setQueryBusinessType(String queryBusinessType) {
        this.queryBusinessType = queryBusinessType;
    }

    public String getQueryOwnerId() {
        return queryOwnerId;
    }

    public void setQueryOwnerId(String queryOwnerId) {
        this.queryOwnerId = queryOwnerId;
    }

    public String getQueryOwnerType() {
        return queryOwnerType;
    }

    public void setQueryOwnerType(String queryOwnerType) {
        this.queryOwnerType = queryOwnerType;
    }

    public String getQueryEnableFlag() {
        return queryEnableFlag;
    }

    public void setQueryEnableFlag(String queryEnableFlag) {
        this.queryEnableFlag = queryEnableFlag;
    }

    public String getQueryPublicFlag() {
        return queryPublicFlag;
    }

    public void setQueryPublicFlag(String queryPublicFlag) {
        this.queryPublicFlag = queryPublicFlag;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryRemarks() {
        return queryRemarks;
    }

    public void setQueryRemarks(String queryRemarks) {
        this.queryRemarks = queryRemarks;
    }
}
