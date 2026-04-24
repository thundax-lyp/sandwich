package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.persistence.AdminDataEntity;
import com.github.thundax.modules.sys.entity.Role;

/**
 * @author wdit
 */
public abstract class BaseRole extends AdminDataEntity<Role> {

    private static final long serialVersionUID = 1L;

    public BaseRole() {
        super();
    }

    public BaseRole(String id) {
        super(id);
    }

    private String name;
    private String adminFlag;
    private String enableFlag;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdminFlag() {
        return adminFlag;
    }

    public void setAdminFlag(String adminFlag) {
        this.adminFlag = adminFlag;
    }

    public String getEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag) {
        this.enableFlag = enableFlag;
    }

}
