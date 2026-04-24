package com.github.thundax.modules.sys.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.Constants;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.MapUtils;
import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.service.UserEncryptService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.utils.RoleServiceHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class UserServiceImpl extends CrudServiceImpl<UserDao, User> implements UserService {

    private static final String CACHE_ROLES_ = ".roles_";
    private final SignService signService;
    private final UserEncryptService userEncryptService;

    /**
     * userId -> roleIdList
     */
    private final PooledThreadLocal<Map<String, List<String>>> uidRidsMapHandler = new PooledThreadLocal<>();

    public UserServiceImpl(UserDao dao, RedisClient redisClient, SignService signService, UserEncryptService userEncryptService) {
        super(dao, redisClient);
        this.signService = signService;
        this.userEncryptService = userEncryptService;
    }

    @Override
    protected boolean isRedisCacheEnabled() {
        return true;
    }

    @Override
    protected String getCacheSection() {
        return Constants.CACHE_PREFIX + "sys.user";
    }

    @Override
    public User getByLoginName(String loginName) {
        User user = dao.getByLoginName(loginName);
        if (user != null) {
            putCache(user);
            userEncryptService.get(user.getId());
        }
        return user;
    }

    @Override
    public User getBySsoLoginName(String ssoLoginName) {
        User user = dao.getBySsoLoginName(ssoLoginName);
        if (user != null) {
            putCache(user);
            userEncryptService.get(user.getId());
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(User user) {
        super.save(user);

        dao.deleteUserRole(user);
        if (ListUtils.isNotEmpty(user.getRoleIdList())) {
            dao.insertUserRole(user);
        }
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
        UserEncrypt userEncrypt = new UserEncrypt();
        userEncrypt.setId(user.getId());
        userEncrypt.setCreateUserId(user.getCreateUserId());
        userEncrypt.setUpdateUserId(user.getUpdateUserId());
        userEncrypt.setEmail(user.getEmail());
        userEncrypt.setMobile(user.getMobile());
        userEncrypt.setTel(user.getTel());
        userEncryptService.save(userEncrypt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(User user) {
        user.preUpdate();
        dao.updateLoginPass(user);
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
        this.removeCache(user);
        UserEncrypt userEncrypt = new UserEncrypt();
        userEncrypt.setId(user.getId());
        userEncrypt.setCreateUserId(user.getCreateUserId());
        userEncrypt.setUpdateUserId(user.getUpdateUserId());
        userEncrypt.setLoginPass(user.getLoginPass());
        userEncryptService.updateLoginPass(userEncrypt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(User user) {
        dao.updateLoginInfo(user);
        this.removeCache(user);
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(User user) {
        user.preUpdate();

        int result = dao.updateEnableFlag(user);

        this.removeCache(user);
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(List<User> list) {
        return batchOperate(list, this::updateEnableFlag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(User user) {
        dao.deleteUserRole(user);

        int result = super.delete(user);

        // 移除关联缓存数据
        RoleServiceHolder.getService().removeAllCache();
        signService.deleteSign(user.getSignName(), user.getSignId());

        return result;
    }

    @Override
    public List<Role> findUserRole(User user) {
        List<String> roleIdList = uidRidsMapHandler
                .computeIfAbsent(MapUtils::newHashMap)
                .computeIfAbsent(String.valueOf(user.getId()),
                        (userId) -> redisClient.computeIfAbsent(getCacheSection() + CACHE_ROLES_ + userId,
                                new TypeReference<List<String>>() {
                                },
                                key -> ListUtils.map(dao.findUserRole(user), Role::getId)));

        return ListUtils.map(roleIdList, Role::new);
    }

    @Override
    protected void removeCache(User user) {
        super.removeCache(user);

        Map<String, List<String>> map = uidRidsMapHandler.get();
        if (map != null) {
            map.remove(user.getId());
        }

        redisClient.delete(getCacheSection() + CACHE_ROLES_ + user.getId());
    }

}
