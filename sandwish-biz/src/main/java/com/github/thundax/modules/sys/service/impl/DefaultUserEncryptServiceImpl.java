package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.modules.sys.dao.UserEncryptDao;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.service.UserEncryptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户重要信息加密默认服务
 *
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class DefaultUserEncryptServiceImpl extends CrudServiceImpl<UserEncryptDao, UserEncrypt>
        implements UserEncryptService {

    public DefaultUserEncryptServiceImpl(UserEncryptDao dao) {
        super(dao);
    }

    /**
     * 更新密码, loginPass, updateDate, updateBy
     *
     * @param userEncrypt 用户
     */
    @Override
    public void updateLoginPass(UserEncrypt userEncrypt) {
        // 默认不做任何处理
    }

    /**
     * 保存
     *
     * @param entity 对象
     */
    @Override
    public void save(UserEncrypt entity) {
        // 默认不做任何处理
    }

    @Override
    public UserEncrypt get(String id) {
        return null;
    }

    @Override
    public UserEncrypt get(UserEncrypt query) {
        return null;
    }

    @Override
    public UserEncrypt findOne(UserEncrypt query) {
        return null;
    }
}
