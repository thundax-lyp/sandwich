package com.github.thundax.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

@ApiModel(value = "Count", description = "统计数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountVo implements Serializable {

    public static final String CATEGORY_TOTAL = "total";
    public static final String CATEGORY_CURRENT = "current";

    private String category;
    private Long count;

    public CountVo() {
        super();
    }

    public CountVo(String category, Long count) {
        this.category = category;
        this.count = count;
    }

    @ApiModelProperty(name = "category", value = "类型")
    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @ApiModelProperty(name = "count", value = "统计数")
    @JsonProperty("count")
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
