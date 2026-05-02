package com.github.thundax.modules.sys.service.query;

import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MenuQuery implements Serializable {
    private String parentId;
    private MenuVisibility visibility;
    private Integer maxRank;

    public void setVisibility(String visibility) {
        this.visibility = StringUtils.isBlank(visibility) ? null : MenuVisibility.from(visibility);
    }

    public void setVisibility(MenuVisibility visibility) {
        this.visibility = visibility;
    }
}
