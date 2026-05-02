package com.github.thundax.modules.sys.service.query;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfficeQuery implements Serializable {
    private String parentId;
    private String name;
    private String remarks;
}
