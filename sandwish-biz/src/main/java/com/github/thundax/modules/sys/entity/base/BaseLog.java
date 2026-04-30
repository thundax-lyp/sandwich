package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseLog implements Signable {
    private EntityId id;

    public BaseLog() {}

    private String userId;

    private String type;
    private Date logDate;
    private String title;
    private String remoteAddr;
    private String userAgent;
    private String method;
    private String requestUri;
    private String requestParams;
    private String remarks;
    private Date createDate;

    @Override
    public String getSignId() {
        return EntityIdCodec.toValue(getId());
    }
}
