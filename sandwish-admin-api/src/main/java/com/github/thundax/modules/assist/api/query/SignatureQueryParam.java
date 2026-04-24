package com.github.thundax.modules.assist.api.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.query.PageQueryParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Size;

/**
 * @author thundax
 */
@ApiModel(value = "SignatureQueryParam", description = "辅助：签名查询参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignatureQueryParam extends PageQueryParam {

    private String businessType;

    @ApiModelProperty(name = "businessType", value = "业务类型")
    @JsonProperty("businessType")
    @Size(max = 64, message = "\"业务类型\"长度不能超过 64")
    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

}
