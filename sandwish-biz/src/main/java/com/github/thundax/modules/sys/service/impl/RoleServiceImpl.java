package com.github.thundax.modules.sys.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.Constants;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.MapUtils;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.RoleDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class RoleServiceImpl extends CrudServiceImpl<RoleDao, Role> implements RoleService {

    private static final String CACHE_USERS_ = "users_";
    private static final String CACHE_MENUS_ = "menus_";
    private final SignService signService;

    /**
     * roleId -> userIdList
     */
    private final PooledThreadLocal<Map<String, List<String>>> idUserIdsMapHandler = new PooledThreadLocal<>();
    /**
     * roleId -> menuIdList
     */
    private final PooledThreadLocal<Map<String, List<String>>> idMenuIdsMapHandler = new PooledThreadLocal<>();

    public RoleServiceImpl(RoleDao dao, RedisClient redisClient, SignService signService) {
        super(dao, redisClient);
        this.signService = signService;
    }

    @Override
    protected boolean isRedisCacheEnabled() {
        return true;
    }

    @Override
    protected String getCacheSection() {
        return Constants.CACHE_PREFIX + "sys.role.";
    }

    @Override
    public List<Role> findValidList() {
        Role query = new Role();
        query.setQueryProp(Role.Query.PROP_ENABLE_FLAG, Global.ENABLE);
        return this.findList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Role role) {
        super.save(role);

        dao.deleteRoleMenu(role);
        if (ListUtils.isNotEmpty(role.getMenuIdList())) {
            dao.insertRoleMenu(role);
        }

        removeCache(role);
        signService.sign(role.getSignName(), role.getSignId(), role.getSignBody());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserList(Role role, List<User> userList) {
        dao.deleteRoleUser(role);

        if (ListUtils.isNotEmpty(userList)) {
            dao.insertRoleUser(role, userList);
        }

        removeCache(role);
        signService.sign(role.getSignName(), role.getSignId(), role.getSignBody());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(Role role) {
        role.preUpdate();

        int result = dao.updateEnableFlag(role);

        removeCache(role);
        signService.sign(role.getSignName(), role.getSignId(), role.getSignBody());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(List<Role> list) {
        return batchOperate(list, this::updateEnableFlag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Role role) {
        dao.deleteRoleMenu(role);
        dao.deleteRoleUser(role);
        int retVal = super.delete(role);

        signService.deleteSign(role.getSignName(), role.getSignId());

        return retVal;
    }

    @Override
    public List<User> findRoleUser(Role role) {
        List<String> userIdList = idUserIdsMapHandler
                .computeIfAbsent(MapUtils::newHashMap)
                .computeIfAbsent(role.getId(),
                        (roleId) -> redisClient.computeIfAbsent(getCacheSection() + CACHE_USERS_ + roleId,
                                new TypeReference<List<String>>() {
                                },
                                (key) -> ListUtils.map(dao.findRoleUser(role), User::getId)));

        return ListUtils.map(userIdList, User::new);
    }

    @Override
    public List<Menu> findRoleMenu(Role role) {
        List<String> menuIdList = idMenuIdsMapHandler
                .computeIfAbsent(MapUtils::newHashMap)
                .computeIfAbsent(String.valueOf(role.getId()),
                        (roleId) -> redisClient.computeIfAbsent(getCacheSection() + CACHE_MENUS_ + roleId,
                                new TypeReference<List<String>>() {
                                },
                                (key) -> ListUtils.map(dao.findRoleMenu(role), Menu::getId)));

        return ListUtils.map(menuIdList, Menu::new);
    }

    @Override
    protected void removeCache(Role role) {
        super.removeCache(role);
        redisClient.delete(getCacheSection() + CACHE_USERS_ + role.getId());
        redisClient.delete(getCacheSection() + CACHE_MENUS_ + role.getId());
        UserServiceHolder.getService().removeAllCache();

        MapUtils.forEach(SpringContextHolder.getBeansOfType(CacheChangedListener.class),
                (name, listener) -> listener.onRoleCacheChanged());
    }

    public interface CacheChangedListener {
        /**
         * role cache changed listener
         */
        void onRoleCacheChanged();
    }

}
