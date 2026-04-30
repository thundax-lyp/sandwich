package com.github.thundax.modules.member.entity.base;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.domain.Sortable;
import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseMember implements Auditable, Sortable {
    private EntityId id;

    private String loginName;
    private String loginPass;

    private String email;
    private String name;
    private String gender;
    private String mobile;
    private String address;
    private String zipcode;

    private String enableFlag;

    private String registerIp;
    private Date registerDate;
    private String lastLoginIp;
    private Date lastLoginDate;

    private String ywtbId;

    private int loginCount;
    private int priority;
    private String remarks;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public BaseMember() {
        initialize();
    }

    protected void initialize() {}

    @Override
    public void setPriority(int priority) {
        this.priority = priority >= 0 ? priority : 0;
    }
}
