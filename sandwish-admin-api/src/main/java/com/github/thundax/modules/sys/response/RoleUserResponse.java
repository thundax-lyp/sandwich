package com.github.thundax.modules.sys.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "RoleUserResponse", description = "角色用户响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleUserResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "用户ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "name", value = "姓名")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(name = "loginName", value = "登录名")
    @JsonProperty("loginName")
    private String loginName;

    @ApiModelProperty(name = "office", value = "归属组织机构")
    @JsonProperty("office")
    private RoleOfficeResponse office;
}
