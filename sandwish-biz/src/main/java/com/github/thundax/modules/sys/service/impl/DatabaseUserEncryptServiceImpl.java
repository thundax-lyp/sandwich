package com.github.thundax.modules.sys.service.impl;

import static com.github.thundax.common.Constants.QUEUE_PREFIX;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.dao.UserEncryptDao;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.service.UserEncryptService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

/**
 * 用户重要信息加密数据库加密服务 数据库列加密
 */
@Service
@Transactional(readOnly = true)
public class DatabaseUserEncryptServiceImpl implements UserEncryptService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String QUEUE_ENCRYPT_ADD = QUEUE_PREFIX + "encrypt.db.add";
    public static final String QUEUE_ENCRYPT_UPDATE = QUEUE_PREFIX + "encrypt.db.update";

    public static final String QUEUE_ENCRYPT_UPDATE_LOGIN_PASS = QUEUE_PREFIX + "encrypt.db.update.login.pass";
    public static final String QUEUE_ENCRYPT_QUERY = QUEUE_PREFIX + "encrypt.db.query";
    private final UserEncryptDao dao;
    protected final AmqpTemplate amqpTemplate;

    public DatabaseUserEncryptServiceImpl(UserEncryptDao dao, AmqpTemplate amqpTemplate) {
        this.dao = dao;
        this.amqpTemplate = amqpTemplate;
    }

    /**
     * 迁移兼容：更新旧用户加密表密码镜像, loginPass, updateDate, updateBy
     */
    @Override
    public void updateLoginPass(UserEncrypt entity) {
        if (entity != null) {
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_UPDATE_LOGIN_PASS, JsonUtils.toJson(entity));
        }
    }

    @Override
    public void add(UserEncrypt entity) {
        if (entity != null) {
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_ADD, JsonUtils.toJson(entity));
        }
    }

    @Override
    public void update(UserEncrypt entity) {
        if (entity != null) {
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_UPDATE, JsonUtils.toJson(entity));
        }
    }

    public UserEncrypt getById(EntityId id) {
        if (id != null) {
            UserEncrypt userEncrypt = new UserEncrypt();
            userEncrypt.setId(id);
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_QUERY, JsonUtils.toJson(userEncrypt));
        }
        return null;
    }

    @RabbitListener(queues = QUEUE_ENCRYPT_ADD, concurrency = "1")
    public void encryptAdd(String encryptEntity) {
        if (StringUtils.isBlank(encryptEntity)) {
            logger.warn("加密内容为空");
            return;
        }

        try {
            UserEncrypt userEncryptPre = JsonUtils.fromJson(encryptEntity, UserEncrypt.class);
            if (userEncryptPre == null) {
                return;
            }
            addDirect(userEncryptPre);

        } catch (RestClientException e) {
            logger.error("加密对象：{}，加密异常：{}", encryptEntity, e.getMessage());
        }
    }

    @RabbitListener(queues = QUEUE_ENCRYPT_UPDATE, concurrency = "1")
    public void encryptUpdate(String encryptEntity) {
        if (StringUtils.isBlank(encryptEntity)) {
            logger.warn("加密内容为空");
            return;
        }

        try {
            UserEncrypt userEncryptPre = JsonUtils.fromJson(encryptEntity, UserEncrypt.class);
            if (userEncryptPre == null) {
                return;
            }
            updateDirect(userEncryptPre);

        } catch (RestClientException e) {
            logger.error("加密对象：{}，加密异常：{}", encryptEntity, e.getMessage());
        }
    }

    @RabbitListener(queues = QUEUE_ENCRYPT_UPDATE_LOGIN_PASS, concurrency = "1")
    public void enctyptUpdateLoginPass(String encryptEntity) {
        if (StringUtils.isBlank(encryptEntity)) {
            logger.warn("加密内容为空");
            return;
        }

        try {
            UserEncrypt userEncryptPre = JsonUtils.fromJson(encryptEntity, UserEncrypt.class);
            if (userEncryptPre == null) {
                return;
            }
            dao.updateLoginPass(userEncryptPre);

        } catch (RestClientException e) {
            logger.error("加密对象：{}，加密异常：{}", encryptEntity, e.getMessage());
        }
    }

    @RabbitListener(queues = QUEUE_ENCRYPT_QUERY, concurrency = "1")
    public void enctyptQuery(String encryptEntity) {
        if (StringUtils.isBlank(encryptEntity)) {
            logger.warn("加密内容为空");
            return;
        }

        try {
            UserEncrypt userEncryptPre = JsonUtils.fromJson(encryptEntity, UserEncrypt.class);
            if (userEncryptPre == null) {
                return;
            }
            dao.getById(userEncryptPre.getId());

        } catch (RestClientException e) {
            logger.error("加密对象：{}，加密异常：{}", encryptEntity, e.getMessage());
        }
    }

    private void addDirect(UserEncrypt entity) {
        dao.insert(entity);
    }

    private void updateDirect(UserEncrypt entity) {
        dao.update(entity);
    }
}
