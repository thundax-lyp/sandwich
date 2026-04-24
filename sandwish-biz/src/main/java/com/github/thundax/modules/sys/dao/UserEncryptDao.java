package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserEncrypt;

import java.util.List;

/**
 * 用户重要信息加密DAO接口
 *
 * @author thundax
 */
@MyBatisDao
public interface UserEncryptDao extends CrudDao<UserEncrypt> {

    /**
     * 更新密码, loginPass, updateDate, updateBy
     *
     * @param userEncrypt 用户
     */
    void updateLoginPass(UserEncrypt userEncrypt);
}
