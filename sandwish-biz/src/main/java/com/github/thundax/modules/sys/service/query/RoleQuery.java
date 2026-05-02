package com.github.thundax.modules.sys.service.query;

import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleQuery implements Serializable {
    private RoleStatus status;
}
