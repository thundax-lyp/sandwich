package com.github.thundax.common.web.request;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class IdRequest implements Serializable {

    @ApiModelProperty(name = "id", value = "ID")
    @NotNull(message = "ID不能为空")
    private Long id;
}
