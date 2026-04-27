package com.github.thundax.modules.assist.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/** @author thundax */
@ApiModel(value = "Signature", description = "辅助：数据签名")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignatureVo extends BaseVo {

    private String businessType;
    private String businessId;
    private String bodyParams;
    private String signature;

    public SignatureVo() {
        super();
    }

    public SignatureVo(String id) {
        super(id);
    }

    @ApiModelProperty(name = "businessType", value = "业务类型")
    @JsonProperty("businessType")
    @NotEmpty(message = "\"业务类型\"不能为空")
    @Size(max = 64, message = "\"业务类型\"长度不能超过 64")
    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    @ApiModelProperty(name = "businessId", value = "业务ID")
    @JsonProperty("businessId")
    @NotEmpty(message = "\"业务ID\"不能为空")
    @Size(max = 64, message = "\"业务ID\"长度不能超过 64")
    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    @ApiModelProperty(name = "bodyParams", value = "原文")
    @JsonProperty("bodyParams")
    public String getBodyParams() {
        return bodyParams;
    }

    public void setBodyParams(String bodyParams) {
        this.bodyParams = bodyParams;
    }

    @ApiModelProperty(name = "signature", value = "签名")
    @JsonProperty("signature")
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
