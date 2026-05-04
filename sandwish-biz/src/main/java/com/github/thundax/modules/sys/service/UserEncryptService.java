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

    /**
     * 迁移兼容：更新旧用户加密表密码镜像, loginPass, updateDate, updateBy
     */
    void updateLoginPass(UserEncrypt userEncrypt);
}
