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
@ApiModel(value = "UserOptionsResponse", description = "用户选项响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserOptionsResponse implements Serializable {

    @ApiModelProperty(name = "statusOptions", value = "用户状态选项")
    private List<OptionResponse> statusOptions = new ArrayList<>();
}
