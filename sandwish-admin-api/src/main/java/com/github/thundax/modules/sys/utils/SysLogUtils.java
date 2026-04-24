package com.github.thundax.modules.sys.utils;

import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.Constants;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.DateUtils;
import com.github.thundax.common.utils.FileUtils;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.sys.entity.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wdit
 */
@Service
@Lazy(value = false)
public class SysLogUtils {

    private static final Logger logger = LoggerFactory.getLogger(SysLogUtils.class);

    public static final String QUEUE_SAVE_LOG = Constants.QUEUE_PREFIX + "save-log";

    public static final DateFormat LOG_FILENAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final String LOG_EXTEND_NAME = ".log";

    private static VltavaProperties.LogProperties properties;

    private static AmqpTemplate template;

    @Autowired
    public SysLogUtils(AmqpTemplate targetTemplate,
                       VltavaProperties vltavaProperties) {
        template = targetTemplate;
        properties = vltavaProperties.getLog();
    }

    public static AmqpTemplate getTemplate() {
        if (template == null) {
            template = SpringContextHolder.getBean(AmqpTemplate.class);
        }
        return template;
    }

    public static VltavaProperties.LogProperties getProperties() {
        if (properties == null) {
            VltavaProperties vltavaProperties = SpringContextHolder.getBean(VltavaProperties.class);
            properties = vltavaProperties.getLog();
        }
        return properties;
    }

    public static void saveLog(Log log) {
        getTemplate().convertAndSend(QUEUE_SAVE_LOG, JsonUtils.toJson(log));
    }

    @RabbitListener(queues = QUEUE_SAVE_LOG, concurrency = "2")
    public void saveLogHandler(String paramString) {
        try {
            Log log = JsonUtils.fromJson(paramString, Log.class);
            if (log != null) {
                LogServiceHolder.getService().save(log);

                try {
                    String filename = LOG_FILENAME_FORMAT.format(log.getLogDate()) + LOG_EXTEND_NAME;

                    FileUtils.writeLines(new File(getProperties().getStoragePath(), filename),
                            ListUtils.newArrayList(paramString),
                            true);

                } catch (Exception e) {
                    logger.warn("can not save sys-log");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());

        } finally {
            PooledThreadLocal.reset();
        }
    }

    /**
     * 删除过期任务
     */
    @Scheduled(cron = "0 0 0/4 * * ?")
    void doTask() {
        Log query = new Log();
        query.setQueryProp(Log.Query.PROP_BEGIN_DATE, DateUtils.addDays(new Date(), -9999));
        query.setQueryProp(Log.Query.PROP_END_DATE, DateUtils.addDays(new Date(), -properties.getAliveDays()));
        LogServiceHolder.getService().batchDelete(query);
    }

}
