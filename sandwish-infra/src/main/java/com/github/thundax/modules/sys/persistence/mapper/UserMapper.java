package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 用户 MyBatis Mapper。 */
@MyBatisDao
public interface UserMapper {

    UserDO get(UserDO user);

    List<UserDO> getMany(@Param("idList") List<String> idList);

    List<UserDO> findList(UserDO user);

    int insert(UserDO user);

    int update(UserDO user);

    int updatePriority(UserDO user);

    int delete(UserDO user);

    UserDO getByLoginName(String loginName);

    UserDO getBySsoLoginName(String ssoLoginName);

    void updateLoginInfo(UserDO user);

    int updateEnableFlag(UserDO user);

    void updateLoginPass(UserDO user);

    List<String> findUserRole(UserDO user);

    void deleteUserRole(UserDO user);

    void insertUserRole(UserDO user);
}
