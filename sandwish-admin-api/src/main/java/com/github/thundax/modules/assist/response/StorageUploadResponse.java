package com.github.thundax.modules.assist.response;

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
    private String id;

    @ApiModelProperty(name = "name", value = "文件名称")
    @JsonProperty("name")
    private String name;

    @ApiModelProperty(name = "extendName", value = "扩展名")
    @JsonProperty("extendName")
    private String extendName;

    @ApiModelProperty(name = "mimeType", value = "MIME-TYPE")
    @JsonProperty("mimeType")
    private String mimeType;

    @ApiModelProperty(name = "url", value = "预览URL")
    @JsonProperty("url")
    private String url;

    @ApiModelProperty(name = "error", value = "错误信息")
    @JsonProperty("error")
    private String error;
}
