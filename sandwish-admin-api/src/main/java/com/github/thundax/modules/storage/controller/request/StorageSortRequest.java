package com.github.thundax.modules.storage.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.domain.SortDirection;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "StorageSortRequest", description = "存储资源排序请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageSortRequest {

    @ApiModelProperty(name = "orderedIds", value = "排序实体ID序列")
    @JsonProperty("orderedIds")
    @NotEmpty(message = "orderedIds不能为空")
    private List<Long> orderedIds;

    @ApiModelProperty(name = "sortDirection", value = "排序方向")
    @JsonProperty("sortDirection")
    private SortDirection sortDirection = SortDirection.ASC;
}
