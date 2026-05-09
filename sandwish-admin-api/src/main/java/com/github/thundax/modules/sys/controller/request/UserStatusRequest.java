package com.github.thundax.modules.sys.controller.request;

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
@ApiModel(value = "UserStatusRequest", description = "用户状态请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserStatusRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "用户ID")
    @JsonProperty("id")
    @NotEmpty(message = "ID不能为空")
    @Size(max = 64, message = "ID长度不能超过64")
    private Long id;

    @ApiModelProperty(name = "enable", value = "启用/禁用")
    @JsonProperty("enable")
    private Boolean enable;
}
