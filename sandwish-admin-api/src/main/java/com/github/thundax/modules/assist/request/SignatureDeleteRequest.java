package com.github.thundax.modules.assist.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "SignatureDeleteRequest", description = "签名删除请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignatureDeleteRequest implements Serializable {

    @ApiModelProperty(name = "businessType", value = "业务类型")
    @JsonProperty("businessType")
    @NotEmpty(message = "\"业务类型\"不能为空")
    @Size(max = 64, message = "\"业务类型\"长度不能超过 64")
    private String businessType;

    @ApiModelProperty(name = "businessId", value = "业务ID")
    @JsonProperty("businessId")
    @NotEmpty(message = "\"业务ID\"不能为空")
    @Size(max = 64, message = "\"业务ID\"长度不能超过 64")
    private String businessId;
}
