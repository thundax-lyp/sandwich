package com.github.thundax.common.persistence.query;

import java.io.Serializable;

/**
 * 自定义查询条件
 *
 * @author thundax
 */
public class QueryAttrParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String type;
    private String operator;
    private String value;

    public QueryAttrParam() {

    }

    public QueryAttrParam(String name, String type, String operator, String value) {
        this.setName(name);
        this.setType(type);
        this.setOperator(operator);
        this.setValue(value);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
