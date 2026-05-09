package com.github.thundax.modules.sys.service.command;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.enums.LogType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateLogCommand {
    private EntityId id;
    private String userId;
    private LogType type;
    private Date logDate;
    private String title;
    private String remoteAddr;
    private String userAgent;
    private String method;
    private String requestUri;
    private String requestParams;
    private String remarks;
}
