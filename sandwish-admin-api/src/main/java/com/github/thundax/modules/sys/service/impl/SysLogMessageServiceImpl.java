package com.github.thundax.modules.sys.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.service.LogService;
import com.github.thundax.modules.sys.service.SysLogMessageService;
import com.github.thundax.modules.sys.service.command.CreateLogCommand;
import com.github.thundax.modules.sys.service.query.LogQuery;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Lazy(value = false)
@Slf4j
@RequiredArgsConstructor
public class SysLogMessageServiceImpl implements SysLogMessageService {

    private static final DateFormat LOG_FILENAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String LOG_EXTEND_NAME = ".log";

    private final AmqpTemplate template;
    private final SandwishProperties sandwishProperties;
    private final LogService logService;
    private final ObjectMapper objectMapper;

    @Override
    public void saveLog(Log sysLog) {
        try {
            template.convertAndSend(QUEUE_SAVE_LOG, objectMapper.writeValueAsString(sysLog));
        } catch (Exception e) {
            log.warn("can not serialize sys-log message", e);
        }
    }

    @RabbitListener(queues = QUEUE_SAVE_LOG, concurrency = "2")
    public void saveLogHandler(String paramString) {
        try {
            Log sysLog = objectMapper.readValue(paramString, Log.class);
            if (sysLog != null) {
                sysLog.setId(logService.create(toCreateCommand(sysLog)));

                try {
                    String filename = LOG_FILENAME_FORMAT.format(sysLog.getLogDate()) + LOG_EXTEND_NAME;
                    File logFile = new File(logProperties().getStoragePath(), filename);

                    FileUtils.writeLines(logFile, new ArrayList<>(Collections.singletonList(paramString)), true);

                } catch (Exception e) {
                    log.warn("can not save sys-log to {}", logProperties().getStoragePath(), e);
                }
            }

        } catch (Exception e) {
            log.error("can not consume sys-log message", e);
        }
    }

    @Scheduled(cron = "0 0 0/4 * * ?")
    void doTask() {
        LogQuery query = new LogQuery();
        query.setBeginDate(DateUtils.addDays(new Date(), -9999));
        query.setEndDate(DateUtils.addDays(new Date(), -logProperties().getAliveDays()));
        logService.deleteByCondition(query);
    }

    private SandwishProperties.LogProperties logProperties() {
        return sandwishProperties.getLog();
    }

    private CreateLogCommand toCreateCommand(Log log) {
        return new CreateLogCommand(
                log.getId(),
                log.getUserId(),
                log.getType(),
                log.getLogDate(),
                log.getTitle(),
                log.getRemoteAddr(),
                log.getUserAgent(),
                log.getMethod(),
                log.getRequestUri(),
                log.getRequestParams(),
                log.getRemarks());
    }
}
