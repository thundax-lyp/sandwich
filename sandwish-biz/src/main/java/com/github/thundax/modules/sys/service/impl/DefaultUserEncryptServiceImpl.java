package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.dao.UserEncryptDao;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.service.UserEncryptService;
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
     */
    @Override
    public void updateLoginPass(UserEncrypt userEncrypt) {
        // 默认不做任何处理
    }

    @Override
    public void add(UserEncrypt entity) {
        // 默认不做任何处理
    }

    @Override
    public void update(UserEncrypt entity) {
        // 默认不做任何处理
    }

    public UserEncrypt getById(EntityId id) {
        return null;
    }
}
