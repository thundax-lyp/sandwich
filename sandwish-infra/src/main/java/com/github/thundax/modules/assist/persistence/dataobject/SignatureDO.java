package com.github.thundax.modules.assist.persistence.dataobject;

import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 签名持久化对象。
 */
@NoArgsConstructor
public class SignatureDO {

    private String id;
    private boolean isNewRecord;

    private String businessType;
    private String businessId;
    private String signature;
    private String isVerifySign;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String delFlag;
    private String queryBusinessType;
    private String queryBusinessId;
    private String queryIsVerifySign;
    private List<String> queryBusinessIdList;

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

    public String getQueryBusinessType() {
        return queryBusinessType;
    }

    public void setQueryBusinessType(String queryBusinessType) {
        this.queryBusinessType = queryBusinessType;
    }

    public String getQueryBusinessId() {
        return queryBusinessId;
    }

    public void setQueryBusinessId(String queryBusinessId) {
        this.queryBusinessId = queryBusinessId;
    }

    public String getQueryIsVerifySign() {
        return queryIsVerifySign;
    }

    public void setQueryIsVerifySign(String queryIsVerifySign) {
        this.queryIsVerifySign = queryIsVerifySign;
    }

    public List<String> getQueryBusinessIdList() {
        return queryBusinessIdList;
    }

    public void setQueryBusinessIdList(List<String> queryBusinessIdList) {
        this.queryBusinessIdList = queryBusinessIdList;
    }
}
