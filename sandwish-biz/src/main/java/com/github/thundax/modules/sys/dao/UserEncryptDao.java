package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import java.util.List;

public interface UserEncryptDao {

    UserEncrypt get(String id);

    List<UserEncrypt> getMany(List<String> idList);

    List<UserEncrypt> findList();

    Page<UserEncrypt> findPage(int pageNo, int pageSize);

    int insert(UserEncrypt userEncrypt);

    int update(UserEncrypt userEncrypt);

    int updatePriority(UserEncrypt userEncrypt);

    int updateDelFlag(UserEncrypt userEncrypt);

    int delete(String id);

    /**
     * 更新密码, loginPass, updateDate, updateBy
     *
     * @param userEncrypt 用户
     */
    void updateLoginPass(UserEncrypt userEncrypt);
}
