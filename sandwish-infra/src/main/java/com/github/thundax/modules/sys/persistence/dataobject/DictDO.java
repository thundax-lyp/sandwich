package com.github.thundax.modules.sys.persistence.dataobject;

import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 字典持久化对象。
 */
@NoArgsConstructor
public class DictDO {

    public static final String DEL_FLAG_NORMAL = "0";

    private String id;
    private boolean isNewRecord;

    private String type;
    private String label;
    private String value;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private String createUserId;
    private Date updateDate;
    private String updateUserId;
    private String delFlag;
    private String queryType;
    private String queryRemarks;
    private String queryLabel;

    public DictDO(String id) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getQueryRemarks() {
        return queryRemarks;
    }

    public void setQueryRemarks(String queryRemarks) {
        this.queryRemarks = queryRemarks;
    }

    public String getQueryLabel() {
        return queryLabel;
    }

    public void setQueryLabel(String queryLabel) {
        this.queryLabel = queryLabel;
    }
}
