package com.github.thundax.modules.sys.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "RoleUserRequest", description = "角色用户请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleUserRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "用户ID")
    @JsonProperty("id")
    @NotEmpty(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64")
    private Long id;
}
