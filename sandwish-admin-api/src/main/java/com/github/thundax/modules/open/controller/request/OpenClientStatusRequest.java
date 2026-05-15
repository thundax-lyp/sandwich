package com.github.thundax.modules.open.controller.request;

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
@ApiModel(value = "OpenClientStatusRequest", description = "开放客户端状态请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenClientStatusRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "开放客户端ID")
    @JsonProperty("id")
    @NotEmpty(message = "开放客户端ID不能为空")
    @Size(max = 64, message = "开放客户端ID长度不能超过64")
    private String id;

    @ApiModelProperty(name = "status", value = "开放客户端状态")
    @JsonProperty("status")
    @NotEmpty(message = "开放客户端状态不能为空")
    @Size(max = 32, message = "开放客户端状态长度不能超过32")
    private String status;
}
