package com.github.thundax.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;

@ApiModel(value = "Page", description = "分页")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageVo<T> implements Serializable {

    private Integer pageNo;
    private Integer pageSize;
    private Integer totalPage;
    private Long count;
    private String orderBy;
    private List<T> records;

    public PageVo() {}

    @ApiModelProperty(name = "pageNo", value = "页码，从 1 开始")
    @JsonProperty("pageNo")
    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    @ApiModelProperty(name = "pageSize", value = "每页数据条数")
    @JsonProperty("pageSize")
    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @ApiModelProperty(name = "totalPage", value = "总页码")
    @JsonProperty("totalPage")
    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    @ApiModelProperty(name = "orderBy", value = "排序规则")
    @JsonProperty("orderBy")
    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    @ApiModelProperty(name = "totalCount", value = "总数据条数")
    @JsonProperty("totalCount")
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @ApiModelProperty(name = "records", value = "当前页的数据集")
    @JsonProperty("records")
    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
