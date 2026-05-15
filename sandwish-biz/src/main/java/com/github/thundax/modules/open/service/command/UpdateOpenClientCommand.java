package com.github.thundax.modules.open.service.command;

import com.github.thundax.modules.open.entity.enums.OpenClientStatus;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOpenClientCommand {
    private OpenClientId id;
    private String name;
    private OpenClientStatus status;
    private String ipWhitelist;
    private Date expiredAt;
    private String remarks;
    private List<String> permissions;
}
