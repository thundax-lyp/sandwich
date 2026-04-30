package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseUser implements Auditable, Signable, Sortable {
    private EntityId id;

    private String officeId;

    private String loginName;
    private String loginPass;
    private String email;
    private String mobile;
    private String tel;
    private String name;
    private Integer ranks;

    private Date registerDate;
    private String registerIp;

    private Date lastLoginDate;
    private String lastLoginIp;
    private Integer loginCount;

    private String superFlag;
    private String adminFlag;
    private String enableFlag;
    private String ssoLoginName;

    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public BaseUser() {
        initialize();
    }

    protected void initialize() {}

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }

    @Override
    public String getSignId() {
        return EntityIdCodec.toValue(getId());
    }
}
