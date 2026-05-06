package com.github.thundax.modules.sys.utils;

import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.common.Constants;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.service.LogService;
import com.github.thundax.modules.sys.service.query.LogQuery;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Lazy(value = false)
@Slf4j
public class SysLogUtils {

    public static final String QUEUE_SAVE_LOG = Constants.QUEUE_PREFIX + "save-log";

    public static final DateFormat LOG_FILENAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final String LOG_EXTEND_NAME = ".log";

    private static SandwishProperties.LogProperties properties;

    private static AmqpTemplate template;
    private final LogService logService;

    @Autowired
    public SysLogUtils(AmqpTemplate targetTemplate, SandwishProperties sandwishProperties, LogService logService) {
        template = targetTemplate;
        properties = sandwishProperties.getLog();
        this.logService = logService;
    }

    public static AmqpTemplate getTemplate() {
        if (template == null) {
            template = SpringContextHolder.getBean(AmqpTemplate.class);
        }
        return template;
    }

    public static SandwishProperties.LogProperties getProperties() {
        if (properties == null) {
            SandwishProperties sandwishProperties = SpringContextHolder.getBean(SandwishProperties.class);
            properties = sandwishProperties.getLog();
        }
        return properties;
    }

    public static void saveLog(Log log) {
        getTemplate().convertAndSend(QUEUE_SAVE_LOG, JsonUtils.toJson(log));
    }

    @RabbitListener(queues = QUEUE_SAVE_LOG, concurrency = "2")
    public void saveLogHandler(String paramString) {
        try {
            Log sysLog = JsonUtils.fromJson(paramString, Log.class);
            if (sysLog != null) {
                logService.add(sysLog);

                try {
                    String filename = LOG_FILENAME_FORMAT.format(sysLog.getLogDate()) + LOG_EXTEND_NAME;
                    File logFile = new File(getProperties().getStoragePath(), filename);

                    FileUtils.writeLines(logFile, new ArrayList<>(Collections.singletonList(paramString)), true);

                } catch (Exception e) {
                    log.warn("can not save sys-log to {}", getProperties().getStoragePath(), e);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());

        } finally {
            PooledThreadLocal.reset();
        }
    }

    @Scheduled(cron = "0 0 0/4 * * ?")
    void doTask() {
        LogQuery query = new LogQuery();
        query.setBeginDate(DateUtils.addDays(new Date(), -9999));
        query.setEndDate(DateUtils.addDays(new Date(), -properties.getAliveDays()));
        logService.batchDelete(query);
    }
}
