package com.github.thundax.modules.sys.service.query;

import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuQuery implements Serializable {
    private String parentId;
    private MenuVisibility visibility;
    private Integer maxRank;
}
