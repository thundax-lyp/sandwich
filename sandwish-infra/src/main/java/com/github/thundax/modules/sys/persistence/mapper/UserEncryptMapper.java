package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.UserEncryptDO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 用户加密信息 MyBatis Mapper。 */
@MyBatisDao
public interface UserEncryptMapper {

    UserEncryptDO get(UserEncryptDO userEncrypt);

    List<UserEncryptDO> getMany(@Param("idList") List<String> idList);

    List<UserEncryptDO> findList(UserEncryptDO userEncrypt);

    int insert(UserEncryptDO userEncrypt);

    int update(UserEncryptDO userEncrypt);

    int updatePriority(UserEncryptDO userEncrypt);

    int updateStatus(UserEncryptDO userEncrypt);

    int updateDelFlag(UserEncryptDO userEncrypt);

    int delete(UserEncryptDO userEncrypt);

    /**
     * 更新密码。
     *
     * @param userEncrypt 用户加密信息
     */
    void updateLoginPass(UserEncryptDO userEncrypt);
}
