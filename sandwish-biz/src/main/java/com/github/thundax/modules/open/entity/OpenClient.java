package com.github.thundax.modules.open.entity;

import com.github.thundax.modules.open.entity.enums.OpenClientStatus;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenClient {
    private OpenClientId id;
    private String name;
    private OpenClientStatus status = OpenClientStatus.ENABLED;
    private String ipWhitelist;
    private Date expiredAt;
    private String remarks;

    public boolean isEnabled() {
        return OpenClientStatus.ENABLED == status;
    }

    public boolean isDisabled() {
        return OpenClientStatus.DISABLED == status;
    }

    public boolean isExpired(Date now) {
        return expiredAt != null && now != null && !expiredAt.after(now);
    }

    public void enable() {
        this.status = OpenClientStatus.ENABLED;
    }

    public void disable() {
        this.status = OpenClientStatus.DISABLED;
    }
}
