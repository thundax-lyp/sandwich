package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import java.util.List;

public interface UserEncryptDao {

    UserEncrypt getById(EntityId id);

    List<UserEncrypt> batchGetByIds(List<String> idList);

    List<UserEncrypt> list();

    Page<UserEncrypt> page(int pageNo, int pageSize);

    int insert(UserEncrypt userEncrypt);

    int update(UserEncrypt userEncrypt);

    int updatePriority(UserEncrypt userEncrypt);

    int deleteById(EntityId id);

    /**
     * 更新密码, loginPass, updateDate, updateBy
     *
     * @param userEncrypt 用户
     */
    void updateLoginPass(UserEncrypt userEncrypt);
}
