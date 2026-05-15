package com.github.thundax.modules.open.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.web.request.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "OpenClientPageRequest", description = "开放客户端分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenClientPageRequest extends PageRequest {

    @ApiModelProperty(name = "name", value = "第三方主体名称")
    @JsonProperty("name")
    @Size(max = 128, message = "第三方主体名称长度不能超过128")
    private String name;

    @ApiModelProperty(name = "status", value = "开放客户端状态")
    @JsonProperty("status")
    @Size(max = 32, message = "开放客户端状态长度不能超过32")
    private String status;
}
