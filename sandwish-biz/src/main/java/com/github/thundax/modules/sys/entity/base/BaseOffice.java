package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.persistence.AdminTreeEntity;
import com.github.thundax.modules.sys.entity.Office;

/** @author wdit */
public abstract class BaseOffice extends AdminTreeEntity<Office> {

    private String parentId;

    private String name;
    private String shortName;

    public BaseOffice() {
        super();
    }

    public BaseOffice(String id) {
        super(id);
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
