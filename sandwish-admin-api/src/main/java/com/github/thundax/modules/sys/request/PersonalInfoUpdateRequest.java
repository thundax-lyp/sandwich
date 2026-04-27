package com.github.thundax.modules.sys.request;

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
@ApiModel(value = "PersonalInfoUpdateRequest", description = "个人资料更新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalInfoUpdateRequest implements Serializable {

    @ApiModelProperty(name = "name", value = "姓名")
    @JsonProperty("name")
    @NotEmpty(message = "\"姓名\"不能为空")
    @Size(max = 50, message = "\"姓名\"长度不能超过 50")
    private String name;

    @ApiModelProperty(name = "email", value = "邮箱")
    @JsonProperty("email")
    @Size(max = 50, message = "\"邮箱\"长度不能超过 50")
    private String email;

    @ApiModelProperty(name = "mobile", value = "手机号")
    @JsonProperty("mobile")
    @Size(max = 30, message = "\"手机号\"长度不能超过 30")
    private String mobile;
}
