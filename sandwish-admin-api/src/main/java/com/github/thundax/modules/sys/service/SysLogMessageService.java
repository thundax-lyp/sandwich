package com.github.thundax.modules.sys.service;

import com.github.thundax.modules.sys.entity.Log;

public interface SysLogMessageService {

    String QUEUE_SAVE_LOG = "sandwish.save-log";
    String TOPIC_SAVE_LOG = "sandwish_save_log";

    void saveLog(Log sysLog);

    void consumeLog(String payload);
}
