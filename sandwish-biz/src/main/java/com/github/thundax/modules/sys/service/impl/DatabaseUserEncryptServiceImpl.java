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
 *
 * @author wdit
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
        return new UserEncrypt(id);
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
    public UserEncrypt get(EntityId id) {
        if (id != null) {
            amqpTemplate.convertAndSend(QUEUE_ENCRYPT_QUERY, JsonUtils.toJson(newEntity(EntityIdCodec.toValue(id))));
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
            dao.get(userEncryptPre.getEntityId());

        } catch (RestClientException e) {
            logger.error("加密对象：{}，加密异常：{}", encryptEntity, e.getMessage());
        }
    }

    @Override
    public List<UserEncrypt> getMany(List<String> ids) {
        return dao.getMany(ids);
    }

    @Override
    public List<UserEncrypt> findList(UserEncrypt entity) {
        return dao.findList();
    }

    @Override
    public UserEncrypt findOne(UserEncrypt query) {
        List<UserEncrypt> list = findList(query);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Page<UserEncrypt> findPage(UserEncrypt entity, Page<UserEncrypt> page) {
        Page<UserEncrypt> normalizedPage = normalizePage(page);
        IPage<UserEncrypt> dataPage = dao.findPage(normalizedPage.getPageNo(), normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    public long count(UserEncrypt entity) {
        List<UserEncrypt> list = findList(entity);
        return list == null ? 0 : list.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(UserEncrypt entity) {
        return entity == null ? 0 : dao.delete(entity.getEntityId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(List<UserEncrypt> list) {
        return batchOperate(list, this::delete);
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

    private int batchOperate(Collection<UserEncrypt> collection, Function<UserEncrypt, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (UserEncrypt userEncrypt : collection) {
                count += operator.apply(userEncrypt);
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
