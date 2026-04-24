package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;

import java.util.List;

/**
 * 用户 MyBatis Mapper。
 */
@MyBatisDao
public interface UserMapper extends CrudDao<UserDO> {

    UserDO getByLoginName(String loginName);

    UserDO getBySsoLoginName(String ssoLoginName);

    void updateLoginInfo(UserDO user);

    int updateEnableFlag(UserDO user);

    void updateLoginPass(UserDO user);

    List<String> findUserRole(UserDO user);

    void deleteUserRole(UserDO user);

    void insertUserRole(UserDO user);
}
