package com.github.thundax.modules.sys.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.common.web.response.OptionResponse;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "RoleOptionsResponse", description = "角色选项响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleOptionsResponse implements Serializable {

    @ApiModelProperty(name = "statusOptions", value = "角色状态选项")
    private List<OptionResponse> statusOptions = new ArrayList<>();

    @ApiModelProperty(name = "privilegeOptions", value = "角色权限选项")
    private List<OptionResponse> privilegeOptions = new ArrayList<>();
}
