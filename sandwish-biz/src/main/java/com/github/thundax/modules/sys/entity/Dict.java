package com.github.thundax.modules.sys.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.modules.sys.entity.base.BaseDict;
import java.io.Serializable;

/** @author wdit */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dict extends BaseDict {

    private static final long serialVersionUID = 1L;

    public Dict() {
        super();
    }

    public Dict(String id) {
        super(id);
    }

    private Query query;

    @JsonIgnore
    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

        private static final long serialVersionUID = 1L;

        public static final String PROP_TYPE = "type";
        public static final String PROP_REMARKS = "remarks";
        public static final String PROP_LABEL = "label";

        private String type;
        private String remarks;
        private String label;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
