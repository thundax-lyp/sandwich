package com.github.thundax.modules.sys.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.common.vo.BaseVo;
import io.swagger.annotations.ApiModel;

/**
 * @author wdit
 */
@ApiModel(value = "DictVo", description = "字段")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictVo extends BaseVo {

    private String type;
    private String label;
    private String value;

    public DictVo() {
        super();
    }

    public DictVo(String id) {
        super(id);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
