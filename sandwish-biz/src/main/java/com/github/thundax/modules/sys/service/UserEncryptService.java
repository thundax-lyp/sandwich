package com.github.thundax.modules.sys.service;

import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.UserEncrypt;

/**
 * 用户重要信息加密
 *
 * @author wdit
 */
public interface UserEncryptService extends CrudService<UserEncrypt> {

    /**
     * 更新密码, loginPass, updateDate, updateBy
     *
     * @param userEncrypt 用户
     */
    void updateLoginPass(UserEncrypt userEncrypt);
}
