package com.github.thundax.modules.storage.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
@ApiModel(value = "MultipartUploadCompleteRequest", description = "分片上传完成请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultipartUploadCompleteRequest implements Serializable {

    @ApiModelProperty(name = "storageType", value = "存储类型")
    @JsonProperty("storageType")
    @Size(max = 32, message = "存储类型长度不能超过32")
    private String storageType;

    @ApiModelProperty(name = "bucketName", value = "存储桶")
    @JsonProperty("bucketName")
    @Size(max = 255, message = "存储桶长度不能超过255")
    private String bucketName;

    @ApiModelProperty(name = "objectKey", value = "对象键")
    @JsonProperty("objectKey")
    @Size(max = 512, message = "对象键长度不能超过512")
    private String objectKey;

    @ApiModelProperty(name = "size", value = "文件大小")
    @JsonProperty("size")
    private Long size;

    @ApiModelProperty(name = "accessEndpoint", value = "访问地址")
    @JsonProperty("accessEndpoint")
    @Size(max = 512, message = "访问地址长度不能超过512")
    private String accessEndpoint;
}
