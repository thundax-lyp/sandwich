package com.github.thundax.common.web.request;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PageRequest implements Serializable {

    @ApiModelProperty(name = "pageNo", value = "页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo;

    @ApiModelProperty(name = "pageSize", value = "单页记录数", example = "10")
    @Min(value = 1, message = "单页记录数不能小于1")
    @Max(value = 500, message = "单页记录数不能超过500")
    private Integer pageSize;
}
