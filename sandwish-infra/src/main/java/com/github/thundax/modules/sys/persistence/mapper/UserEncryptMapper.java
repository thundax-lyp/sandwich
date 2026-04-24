package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.UserEncryptDO;

/**
 * 用户加密信息 MyBatis Mapper。
 */
@MyBatisDao
public interface UserEncryptMapper extends CrudDao<UserEncryptDO> {

    /**
     * 更新密码。
     *
     * @param userEncrypt 用户加密信息
     */
    void updateLoginPass(UserEncryptDO userEncrypt);
}
