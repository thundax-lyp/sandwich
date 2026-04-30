package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseUserEncrypt implements Auditable {
    private EntityId id;

    private String loginPass;
    private String email;
    private String mobile;
    private String tel;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public BaseUserEncrypt() {
        initialize();
    }

    protected void initialize() {}
}
