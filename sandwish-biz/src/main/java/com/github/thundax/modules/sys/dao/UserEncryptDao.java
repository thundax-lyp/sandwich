package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.modules.sys.entity.UserEncrypt;

/**
 * 用户重要信息加密DAO接口
 *
 * @author thundax
 */
public interface UserEncryptDao extends CrudDao<UserEncrypt> {

    /**
     * 更新密码, loginPass, updateDate, updateBy
     *
     * @param userEncrypt 用户
     */
    void updateLoginPass(UserEncrypt userEncrypt);
}
