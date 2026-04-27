package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ApiModel(value = "PersonalAvatarUploadRequest", description = "个人头像上传请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalAvatarUploadRequest implements Serializable {

    @ApiModelProperty(name = "avatar", value = "头像文件", required = true)
    @NotNull(message = "\"头像文件\"不能为空")
    private MultipartFile avatar;
}
