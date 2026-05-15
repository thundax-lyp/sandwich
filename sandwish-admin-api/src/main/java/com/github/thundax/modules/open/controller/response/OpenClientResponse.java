package com.github.thundax.modules.open.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "OpenClientResponse", description = "开放客户端响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenClientResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "开放客户端ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "name", value = "第三方主体名称")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(name = "status", value = "开放客户端状态")
    @JsonProperty("status")
    private String status;

    @ApiModelProperty(name = "apiKey", value = "API KEY")
    @JsonProperty("apiKey")
    private String apiKey;

    @ApiModelProperty(name = "ipWhitelist", value = "IP白名单JSON数组字符串")
    @JsonProperty("ipWhitelist")
    private String ipWhitelist;

    @ApiModelProperty(name = "expiredAt", value = "过期时间")
    @JsonProperty("expiredAt")
    private Date expiredAt;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    private String remarks;

    @ApiModelProperty(name = "permissions", value = "权限码列表")
    @JsonProperty("permissions")
    private List<String> permissions;
}
