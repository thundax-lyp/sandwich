package com.github.thundax.modules.sys.service.impl;

import static com.github.thundax.common.Constants.QUEUE_PREFIX;

import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.thundax.modules.sys.dao.UserEncryptDao;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.service.UserEncryptService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

/**
 * 用户重要信息加密数据库加密服务 数据库列加密
 *
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class DatabaseUserEncryptServiceImpl extends CrudServiceImpl<UserEncryptDao, UserEncrypt>
        implements UserEncryptService {

    /** 加密保存队列名称 * */
    public static final String QUEUE_ENCRYPT_SAVE = QUEUE_PREFIX + "encrypt.db.save";

    public static final String QUEUE_ENCRYPT_UPDATE_LOGIN_PASS =
            QUEUE_PREFIX + "encrypt.db.update.login.pass";
    public static final String QUEUE_ENCRYPT_QUERY = QUEUE_PREFIX + "encrypt.db.query";
    protected final AmqpTemplate amqpTemplate;

    public DatabaseUserEncryptServiceImpl(UserEncryptDao dao, AmqpTemplate amqpTemplate) {
        super(dao);
        this.amqpTemplate = amqpTemplate;
    }

    /**
     * 更新密码, loginPass, updateDate, updateBy
     *
     * @param entity 用户重要信息
     */
    @Override
    public void updateLoginPass(UserEncrypt entity) {
        if (entity != null) {
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_UPDATE_LOGIN_PASS, JsonUtils.toJson(entity));
        }
    }

    @Override
    public void save(UserEncrypt entity) {
        if (entity != null) {
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_SAVE, JsonUtils.toJson(entity));
        }
    }

    @Override
    public UserEncrypt get(String id) {
        if (StringUtils.isNotBlank(id)) {
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_QUERY, JsonUtils.toJson(newEntity(id)));
        }
        return null;
    }

    @Override
    public UserEncrypt get(UserEncrypt query) {
        if (query != null) {
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_QUERY, JsonUtils.toJson(query));
        }
        return null;
    }

    @RabbitListener(queues = QUEUE_ENCRYPT_SAVE, concurrency = "1")
    public void enctyptSave(String encryptEntity) {
        if (StringUtils.isBlank(encryptEntity)) {
            logger.warn("加密内容为空");
            return;
        }

        try {
            UserEncrypt userEncryptPre = JsonUtils.fromJson(encryptEntity, UserEncrypt.class);
            if (userEncryptPre == null) {
                return;
            }
            super.save(userEncryptPre);

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
            userEncryptPre.preUpdate();
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
            super.get(userEncryptPre);

        } catch (RestClientException e) {
            logger.error("加密对象：{}，加密异常：{}", encryptEntity, e.getMessage());
        }
    }
}
