package com.github.thundax.modules.sys.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "PersonalAvatarUploadRequest", description = "个人头像上传请求")
public class PersonalAvatarUploadRequest implements Serializable {

    @ApiModelProperty(name = "avatar", value = "头像文件", required = true)
    @NotNull(message = "\"头像文件\"不能为空")
    private MultipartFile avatar;
}
