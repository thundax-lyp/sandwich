package com.github.thundax.modules.sys.service.query;

import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class RoleQuery implements Serializable {
    private RoleStatus status;

    public void setStatus(String status) {
        this.status = StringUtils.isBlank(status) ? null : RoleStatus.from(status);
    }

    public void setStatus(RoleStatus status) {
        this.status = status;
    }
}
