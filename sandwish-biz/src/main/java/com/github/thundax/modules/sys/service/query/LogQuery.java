package com.github.thundax.modules.sys.service.query;

import com.github.thundax.modules.sys.entity.enums.LogType;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogQuery implements Serializable {
    private LogType type;
    private String remoteAddr;
    private String title;
    private String requestUri;
    private String userLoginName;
    private String userName;
    private Date beginDate;
    private Date endDate;
}
