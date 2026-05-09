package com.github.thundax.modules.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "AuditSnapshotResponse", description = "审计快照响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditSnapshotResponse implements Serializable {

    @ApiModelProperty(name = "objectType", value = "对象类型")
    private String objectType;

    @ApiModelProperty(name = "objectId", value = "对象ID")
    private String objectId;

    @ApiModelProperty(name = "displayName", value = "展示名")
    private String displayName;

    @ApiModelProperty(name = "fields", value = "字段")
    private List<AuditSnapshotFieldResponse> fields = new ArrayList<>();
}
