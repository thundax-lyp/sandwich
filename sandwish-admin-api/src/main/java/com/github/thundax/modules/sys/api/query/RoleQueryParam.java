package com.github.thundax.modules.sys.api.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

/**
 * RoleQueryParam
 *
 * @author wdit
 */
@ApiModel(value = "RoleQueryParam", description = "权限查询参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleQueryParam implements Serializable {

    private Boolean enable;

    @ApiModelProperty(name = "enable", value = "启用/禁用")
    @JsonProperty("enable")
    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
