package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.sys.dao.UserEncryptDao;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.service.UserEncryptService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户重要信息加密默认服务
 */
@Service
@Transactional(readOnly = true)
public class DefaultUserEncryptServiceImpl implements UserEncryptService {

    public DefaultUserEncryptServiceImpl(UserEncryptDao dao) {}

    /**
     * 更新密码, loginPass, updateDate, updateBy
     *
     * @param userEncrypt 用户
     */
    @Override
    public void updateLoginPass(UserEncrypt userEncrypt) {
        // 默认不做任何处理
    }

    /**
     * 新增
     *
     * @param entity 对象
     */
    @Override
    public void add(UserEncrypt entity) {
        // 默认不做任何处理
    }

    @Override
    public void update(UserEncrypt entity) {
        // 默认不做任何处理
    }

    @Override
    public UserEncrypt getById(EntityId id) {
        return null;
    }

    @Override
    public UserEncrypt getById(UserEncrypt query) {
        return null;
    }

    @Override
    public UserEncrypt getOne(UserEncrypt query) {
        return null;
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

    @Override
    public List<UserEncrypt> batchGetByIds(List<String> ids) {
        return null;
    }

    @Override
    public List<UserEncrypt> list(UserEncrypt entity) {
        return null;
    }

    @Override
    public Page<UserEncrypt> page(UserEncrypt entity, Page<UserEncrypt> page) {
        return page;
    }

    @Override
    public long count(UserEncrypt entity) {
        return 0;
    }

    @Override
    public int deleteById(UserEncrypt entity) {
        return 0;
    }

    @Override
    public int batchDeleteById(List<UserEncrypt> list) {
        return 0;
    }

    @Override
    public int updatePriority(UserEncrypt entity) {
        return 0;
    }

    @Override
    public int updatePriority(List<UserEncrypt> list) {
        return 0;
    }
}
