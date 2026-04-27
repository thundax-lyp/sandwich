package com.github.thundax.common.vo.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.NotEmpty;

/** @author wdit */
@ApiModel(value = "MoveTreeNodeQueryParam", description = "移动树节点参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoveTreeNodeQueryParam implements Serializable {

    public static final String TYPE_BEFORE = "before";
    public static final String TYPE_AFTER = "after";
    public static final String TYPE_INSIDE = "inside";
    public static final String TYPE_INSIDE_LAST = "insideLast";

    private String fromNodeId;
    private String toNodeId;
    private String type;

    public MoveTreeNodeQueryParam() {
        this.type = TYPE_AFTER;
    }

    @ApiModelProperty(name = "fromNodeId", value = "源节点")
    @JsonProperty("fromNodeId")
    @NotEmpty(message = "\"源节点\"不能为空")
    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    @ApiModelProperty(name = "toNodeId", value = "目标节点")
    @JsonProperty("toNodeId")
    @NotEmpty(message = "\"目标节点\"不能为空")
    public String getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(String toNodeId) {
        this.toNodeId = toNodeId;
    }

    @ApiModelProperty(
            name = "type",
            value =
                    "操作。"
                            + TYPE_BEFORE
                            + ": 节点前;"
                            + TYPE_AFTER
                            + ": 节点后;"
                            + TYPE_INSIDE
                            + ": 成为第一个子节点;"
                            + TYPE_INSIDE_LAST
                            + "成为最后一个子节点",
            allowableValues =
                    ""
                            + TYPE_BEFORE
                            + ","
                            + TYPE_AFTER
                            + ","
                            + TYPE_INSIDE
                            + ","
                            + TYPE_INSIDE_LAST,
            example = TYPE_AFTER)
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
