package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.config.Global;
import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseUser implements Auditable, Signable, Sortable {
    private EntityId id;

    private String officeId;

    private String loginName;
    private String loginPass;
    private String email;
    private String mobile;
    private String tel;
    private String name;
    private Integer ranks = 0;

    private Date registerDate;
    private String registerIp;

    private Date lastLoginDate;
    private String lastLoginIp;
    private Integer loginCount = 0;

    private String superFlag = Global.NO;
    private String adminFlag = Global.NO;
    private String enableFlag;
    private String ssoLoginName;

    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }

    @Override
    public String getSignId() {
        return EntityIdCodec.toValue(getId());
    }
}
