package com.github.thundax.modules.storage.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/** @author thundax */
@ApiModel(value = "Storage", description = "存储资源")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageVo extends BaseVo {

    private String name;
    private String extendName;
    private String mimeType;

    private String businessId;
    private String businessType;
    private String businessParams;

    private String ownerId;
    private String ownerType;

    private Boolean isEnable;
    private Boolean isPublic;

    private String url;

    public StorageVo() {
        super();
    }

    public StorageVo(String id) {
        super(id);
    }

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(name = "extendName", value = "扩展名")
    @JsonProperty("extendName")
    public String getExtendName() {
        return extendName;
    }

    public void setExtendName(String extendName) {
        this.extendName = extendName;
    }

    @ApiModelProperty(name = "mimeType", value = "MIME-TYPE")
    @JsonProperty("mimeType")
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @ApiModelProperty(name = "businessId", value = "业务ID")
    @JsonProperty("businessId")
    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    @ApiModelProperty(name = "businessType", value = "业务类型")
    @JsonProperty("businessType")
    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    @ApiModelProperty(name = "businessParams", value = "业务参数")
    @JsonProperty("businessParams")
    public String getBusinessParams() {
        return businessParams;
    }

    public void setBusinessParams(String businessParams) {
        this.businessParams = businessParams;
    }

    @ApiModelProperty(name = "ownerId", value = "归属人ID")
    @JsonProperty("ownerId")
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @ApiModelProperty(name = "ownerType", value = "归属人类型")
    @JsonProperty("ownerType")
    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    @ApiModelProperty(name = "enable", value = "启用/禁用")
    @JsonProperty("enable")
    public Boolean getEnable() {
        return isEnable;
    }

    public void setEnable(Boolean enable) {
        isEnable = enable;
    }

    @ApiModelProperty(name = "public", value = "公开/私有")
    @JsonProperty("public")
    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    @ApiModelProperty(name = "url", value = "URL")
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
