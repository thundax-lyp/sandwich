package com.github.thundax.modules.sys.service.impl;

import static com.github.thundax.common.Constants.QUEUE_PREFIX;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.JsonUtils;
import com.github.thundax.modules.sys.dao.UserEncryptDao;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.service.UserEncryptService;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
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

    @Override
    public Class<UserEncrypt> getElementType() {
        return UserEncrypt.class;
    }

    @Override
    public UserEncrypt newEntity(String id) {
        UserEncrypt userEncrypt = new UserEncrypt();
        userEncrypt.setId(EntityIdCodec.toDomain(id));
        return userEncrypt;
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

    @Override
    public UserEncrypt getById(EntityId id) {
        if (id != null) {
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_QUERY, JsonUtils.toJson(newEntity(EntityIdCodec.toValue(id))));
        }
        return null;
    }

    @Override
    public UserEncrypt getById(UserEncrypt query) {
        if (query != null) {
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_QUERY, JsonUtils.toJson(query));
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

    @Override
    public List<UserEncrypt> batchGetByIds(List<EntityId> ids) {
        return dao.batchGetByIds(EntityIdCodec.toValues(ids));
    }

    @Override
    public List<UserEncrypt> list(UserEncrypt entity) {
        return dao.list();
    }

    @Override
    public UserEncrypt getOne(UserEncrypt query) {
        List<UserEncrypt> list = list(query);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Page<UserEncrypt> page(UserEncrypt entity, Page<UserEncrypt> page) {
        Page<UserEncrypt> normalizedPage = normalizePage(page);
        IPage<UserEncrypt> dataPage = dao.page(normalizedPage.getPageNo(), normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    public long count(UserEncrypt entity) {
        List<UserEncrypt> list = list(entity);
        return list == null ? 0 : list.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        return id == null ? 0 : dao.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(UserEncrypt entity) {
        return dao.updatePriority(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<UserEncrypt> list) {
        return batchOperate(list, this::updatePriority);
    }

    private void addDirect(UserEncrypt entity) {
        dao.insert(entity);
    }

    private void updateDirect(UserEncrypt entity) {
        dao.update(entity);
    }

    private <T> int batchOperate(Collection<T> collection, Function<T, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (T entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private Page<UserEncrypt> normalizePage(Page<UserEncrypt> page) {
        Page<UserEncrypt> normalizedPage = page == null ? new Page<>() : page;
        if (normalizedPage.getPageNo() < Page.FIRST_PAGE_INDEX) {
            normalizedPage.setPageNo(Page.FIRST_PAGE_INDEX);
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(Page.DEFAULT_PAGE_SIZE);
        }
        return normalizedPage;
    }
}
