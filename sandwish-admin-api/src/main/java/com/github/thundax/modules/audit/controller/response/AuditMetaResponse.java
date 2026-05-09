package com.github.thundax.modules.audit.controller.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "AuditMetaResponse", description = "审计元数据响应")
public class AuditMetaResponse implements Serializable {

    @ApiModelProperty(name = "id", value = "ID")
    private Long id;

    @ApiModelProperty(name = "objectType", value = "对象类型")
    private String objectType;

    @ApiModelProperty(name = "objectId", value = "对象ID")
    private String objectId;

    @ApiModelProperty(name = "version", value = "审计版本")
    private Long version;

    @ApiModelProperty(name = "lastAction", value = "最后动作")
    private String lastAction;

    @ApiModelProperty(name = "lastOperatorName", value = "最后操作者")
    private String lastOperatorName;

    @ApiModelProperty(name = "lastOperatedAt", value = "最后操作时间")
    private Date lastOperatedAt;

    @ApiModelProperty(name = "createdAt", value = "审计创建时间")
    private Date createdAt;
}
