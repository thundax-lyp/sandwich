package com.github.thundax.modules.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "AuditLogDetailResponse", description = "审计日志详情响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLogDetailResponse extends AuditLogResponse implements Serializable {

    @ApiModelProperty(name = "idempotencyKey", value = "幂等键")
    private String idempotencyKey;

    @ApiModelProperty(name = "previousVersion", value = "上一版本")
    private Long previousVersion;

    @ApiModelProperty(name = "beforeSnapshot", value = "变更前快照")
    private AuditSnapshotResponse beforeSnapshot;

    @ApiModelProperty(name = "afterSnapshot", value = "变更后快照")
    private AuditSnapshotResponse afterSnapshot;
}
