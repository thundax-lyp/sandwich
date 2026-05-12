package com.github.thundax.modules.sys.service;

import com.github.thundax.common.Constants;
import com.github.thundax.modules.sys.entity.Log;

public interface SysLogMessageService {

    String QUEUE_SAVE_LOG = Constants.QUEUE_PREFIX + "save-log";

    void saveLog(Log sysLog);

    void consumeLog(String payload);
}
