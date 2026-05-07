package com.github.thundax.modules.storage.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "MultipartUploadInitRequest", description = "分片上传初始化请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultipartUploadInitRequest implements Serializable {

    @ApiModelProperty(name = "businessType", value = "业务类型")
    @JsonProperty("businessType")
    @Size(max = 64, message = "业务类型长度不能超过64")
    private String businessType;

    @ApiModelProperty(name = "originalFilename", value = "文件名称", required = true)
    @JsonProperty("originalFilename")
    @NotEmpty(message = "文件名称不能为空")
    @Size(max = 255, message = "文件名称长度不能超过255")
    private String originalFilename;

    @ApiModelProperty(name = "mimeType", value = "MIME-TYPE")
    @JsonProperty("mimeType")
    @Size(max = 128, message = "MIME-TYPE长度不能超过128")
    private String mimeType;

    @ApiModelProperty(name = "totalSize", value = "文件总大小")
    @JsonProperty("totalSize")
    @Min(value = 1, message = "文件总大小必须大于0")
    private Long totalSize;

    @ApiModelProperty(name = "partSize", value = "分片大小")
    @JsonProperty("partSize")
    @Min(value = 1, message = "分片大小必须大于0")
    private Long partSize;
}
