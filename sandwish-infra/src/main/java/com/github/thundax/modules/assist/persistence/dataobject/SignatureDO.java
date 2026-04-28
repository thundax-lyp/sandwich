package com.github.thundax.modules.assist.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 签名持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_assist_signature")
public class SignatureDO {

    @TableField(exist = false)
    private String id;

    @TableField(exist = false)
    private boolean isNewRecord;

    private String businessType;
    private String businessId;
    private String signature;
    private String isVerifySign;

    @TableField(exist = false)
    private Integer priority;

    @TableField(exist = false)
    private String remarks;

    private Date createDate;
    private Date updateDate;

    @TableField(exist = false)
    private String delFlag;

    public SignatureDO(String id) {
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

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getIsVerifySign() {
        return isVerifySign;
    }

    public void setIsVerifySign(String isVerifySign) {
        this.isVerifySign = isVerifySign;
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
