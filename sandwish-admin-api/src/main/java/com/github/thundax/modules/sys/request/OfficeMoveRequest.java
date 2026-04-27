package com.github.thundax.modules.sys.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "OfficeMoveRequest", description = "机构树节点移动请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficeMoveRequest implements Serializable {

    public static final String TYPE_BEFORE = "before";
    public static final String TYPE_AFTER = "after";
    public static final String TYPE_INSIDE = "inside";
    public static final String TYPE_INSIDE_LAST = "insideLast";

    @ApiModelProperty(name = "fromNodeId", value = "源节点")
    @JsonProperty("fromNodeId")
    @NotEmpty(message = "\"源节点\"不能为空")
    private String fromNodeId;

    @ApiModelProperty(name = "toNodeId", value = "目标节点")
    @JsonProperty("toNodeId")
    @NotEmpty(message = "\"目标节点\"不能为空")
    private String toNodeId;

    @ApiModelProperty(name = "type", value = "操作", example = TYPE_AFTER)
    @JsonProperty("type")
    private String type = TYPE_AFTER;
}
