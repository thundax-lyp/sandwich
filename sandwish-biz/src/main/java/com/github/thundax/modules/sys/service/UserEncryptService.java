package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.UserEncrypt;

/**
 * 用户重要信息加密
 */
public interface UserEncryptService {

    UserEncrypt getById(EntityId id);

    void add(UserEncrypt userEncrypt);

    void update(UserEncrypt userEncrypt);

    void updateLoginPass(UserEncrypt userEncrypt);
}
