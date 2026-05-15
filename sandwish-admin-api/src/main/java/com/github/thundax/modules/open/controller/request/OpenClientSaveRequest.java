package com.github.thundax.modules.open.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "OpenClientSaveRequest", description = "开放客户端保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenClientSaveRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "开放客户端ID")
    @JsonProperty("id")
    @Size(max = 64, message = "开放客户端ID长度不能超过64")
    private String id;

    @ApiModelProperty(name = "name", value = "第三方主体名称")
    @JsonProperty("name")
    @NotEmpty(message = "第三方主体名称不能为空")
    @Size(max = 128, message = "第三方主体名称长度不能超过128")
    private String name;

    @ApiModelProperty(name = "ipWhitelist", value = "IP白名单JSON数组字符串")
    @JsonProperty("ipWhitelist")
    private String ipWhitelist;

    @ApiModelProperty(name = "expiredAt", value = "过期时间")
    @JsonProperty("expiredAt")
    private Date expiredAt;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    @Size(max = 255, message = "备注长度不能超过255")
    private String remarks;

    @ApiModelProperty(name = "permissions", value = "权限码列表")
    @JsonProperty("permissions")
    private List<String> permissions;
}
