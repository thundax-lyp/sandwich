package com.github.thundax.modules.storage.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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

    @TableField(exist = false)
    public static final String DEL_FLAG_NORMAL = "0";

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField(exist = false)
    private boolean isNewRecord;

    private String name;

    @TableField("extend_name")
    private String extendName;

    @TableField("mime_type")
    private String mimeType;

    @TableField(exist = false)
    private String businessId;

    @TableField(exist = false)
    private String businessType;

    @TableField(exist = false)
    private String businessParams;

    @TableField("owner_id")
    private String ownerId;

    @TableField("owner_type")
    private String ownerType;

    @TableField("enable_flag")
    private String enableFlag;

    @TableField("public_flag")
    private String publicFlag;

    private Integer priority;
    private String remarks;

    @TableField("create_date")
    private Date createDate;

    @TableField("update_date")
    private Date updateDate;

    @TableField("del_flag")
    private String delFlag;

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
}
