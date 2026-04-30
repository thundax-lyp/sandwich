package com.github.thundax.modules.sys.entity;

import com.github.thundax.modules.sys.entity.base.BaseDict;
import java.io.Serializable;

public class Dict extends BaseDict {

    public Dict() {
        super();
    }

    public Dict(String id) {
        super(id);
    }

    private Query query;

    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public static class Query implements Serializable {

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
