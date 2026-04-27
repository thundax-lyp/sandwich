package com.github.thundax.modules.sys.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "LogResponse", description = "日志响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "日志ID")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(name = "remarks", value = "备注")
    @JsonProperty("remarks")
    private String remarks;

    @ApiModelProperty(name = "createDate", value = "创建时间")
    @JsonProperty("createDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    @ApiModelProperty(name = "type", value = "类型")
    @JsonProperty("type")
    private String type;

    @ApiModelProperty(name = "title", value = "标题")
    @JsonProperty("title")
    private String title;

    @ApiModelProperty(name = "remoteAddr", value = "来源地址")
    @JsonProperty("remoteAddr")
    private String remoteAddr;

    @ApiModelProperty(name = "userAgent", value = "UA")
    @JsonProperty("userAgent")
    private String userAgent;

    @ApiModelProperty(name = "method", value = "HTTP方法")
    @JsonProperty("method")
    private String method;

    @ApiModelProperty(name = "requestUri", value = "访问地址")
    @JsonProperty("requestUri")
    private String requestUri;

    @ApiModelProperty(name = "requestParams", value = "访问参数")
    @JsonProperty("requestParams")
    private String requestParams;

    @ApiModelProperty(name = "createUser", value = "创建人")
    @JsonProperty("createUser")
    private LogUserResponse createUser;
}
