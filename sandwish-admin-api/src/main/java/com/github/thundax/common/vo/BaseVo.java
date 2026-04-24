package com.github.thundax.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * @author wdit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private Integer priority;
    private String remarks;

    private UserVo createUser;
    private Date createDate;
    private UserVo updateUser;
    private Date updateDate;

    public BaseVo() {

    }

    public BaseVo(String id) {
        this.id = id;
    }

    @ApiModelProperty(name = "id", value = "ID，唯一")
    @JsonProperty("id")
    @Size(max = 64, message = "ID长度不能超过64")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @ApiModelProperty(name = "priority", value = "排序数", example = "0")
    @JsonProperty("priority")
    @Min(value = 0, message = "\"排序数\"必须不能小于 0")
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }


    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    @Size(max = 200, message = "\"备注\"长度不能超过 200")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


    @ApiModelProperty(name = "createUser", value = "创建人")
    @JsonProperty("createUser")
    public UserVo getCreateUser() {
        return createUser;
    }

    public void setCreateUser(UserVo createUser) {
        this.createUser = createUser;
    }


    @ApiModelProperty(name = "createDate", value = "创建时间")
    @JsonProperty("createDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @ApiModelProperty(name = "updateUser", value = "修改人")
    @JsonProperty("updateUser")
    public UserVo getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(UserVo updateUser) {
        this.updateUser = updateUser;
    }


    @ApiModelProperty(name = "updateDate", value = "修改时间")
    @JsonProperty("updateDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
