package com.github.thundax.modules.sys.service;

import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.UserEncrypt;

/**
 * 用户重要信息加密
 */
public interface UserEncryptService extends CrudService<UserEncrypt> {

    /**
     * 迁移兼容：更新旧用户加密表密码镜像, loginPass, updateDate, updateBy
     */
    void updateLoginPass(UserEncrypt userEncrypt);
}
