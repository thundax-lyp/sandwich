package com.github.thundax.common.web.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "Page", description = "分页")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> implements Serializable {

    @ApiModelProperty(name = "pageNo", value = "页码，从 1 开始")
    private int pageNo;

    @ApiModelProperty(name = "pageSize", value = "每页数据条数")
    private int pageSize;

    @ApiModelProperty(name = "totalPage", value = "总页码")
    private int totalPage;

    @ApiModelProperty(name = "totalCount", value = "总数据条数")
    private long count;

    @ApiModelProperty(name = "records", value = "当前页的数据集")
    private List<T> records = new ArrayList<>();
}
