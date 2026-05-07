package com.github.thundax.modules.assist.controller.response;

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
@ApiModel(value = "StorageUploadResponse", description = "存储上传响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageUploadResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "存储资源ID")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty(name = "originalFilename", value = "文件名称")
    @JsonProperty("originalFilename")
    private String originalFilename;

    @ApiModelProperty(name = "extendName", value = "扩展名")
    @JsonProperty("extendName")
    private String extendName;

    @ApiModelProperty(name = "contentType", value = "MIME-TYPE")
    @JsonProperty("contentType")
    private String contentType;

    @ApiModelProperty(name = "contentUrl", value = "预览URL")
    @JsonProperty("contentUrl")
    private String contentUrl;

    @ApiModelProperty(name = "error", value = "错误信息")
    @JsonProperty("error")
    private String error;
}
