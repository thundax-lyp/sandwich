package com.github.thundax.common.vo.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * @author wdit
 */
@ApiModel(value = "PageQueryParam", description = "分页查询参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageQueryParam implements Serializable {

    private Integer pageNo;
    private Integer pageSize;

    public PageQueryParam() {
        this.pageNo = 1;
        this.pageSize = 10;
    }

    @ApiModelProperty(name = "pageNo", value = "页码，从1开始", example = "1")
    @JsonProperty("pageNo")
    @Min(value = 1, message = "页码不能小于1")
    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }


    @ApiModelProperty(name = "pageSize", value = "单页记录数", example = "5")
    @JsonProperty("pageSize")
    @Min(value = 1, message = "单页记录数不能小于1")
    @Max(value = 500, message = "单页记录数不能超过500")
    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

}
