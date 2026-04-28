package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.sys.dao.UserEncryptDao;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.service.UserEncryptService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户重要信息加密默认服务
 *
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class DefaultUserEncryptServiceImpl implements UserEncryptService {

    public DefaultUserEncryptServiceImpl(UserEncryptDao dao) {}

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
     * 新增
     *
     * @param entity 对象
     */
    @Override
    public void add(UserEncrypt entity) {
        // 默认不做任何处理
    }

    @Override
    public void update(UserEncrypt entity) {
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

    @Override
    public Class<UserEncrypt> getElementType() {
        return UserEncrypt.class;
    }

    @Override
    public UserEncrypt newEntity(String id) {
        return new UserEncrypt(id);
    }

    @Override
    public List<UserEncrypt> getMany(List<String> ids) {
        return null;
    }

    @Override
    public List<UserEncrypt> findList(UserEncrypt entity) {
        return null;
    }

    @Override
    public Page<UserEncrypt> findPage(UserEncrypt entity, Page<UserEncrypt> page) {
        return page;
    }

    @Override
    public long count(UserEncrypt entity) {
        return 0;
    }

    @Override
    public int delete(UserEncrypt entity) {
        return 0;
    }

    @Override
    public int delete(List<UserEncrypt> list) {
        return 0;
    }

    @Override
    public int updatePriority(UserEncrypt entity) {
        return 0;
    }

    @Override
    public int updatePriority(List<UserEncrypt> list) {
        return 0;
    }
}
