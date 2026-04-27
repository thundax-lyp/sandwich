package com.github.thundax.modules.sys.service.impl;

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
    public List<Role> findValidList() {
        Role query = new Role();
        Role.Query queryCondition = new Role.Query();
        queryCondition.setEnableFlag(Global.ENABLE);
        query.setQuery(queryCondition);
        return this.findList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Role role) {
        if (role.getIsNewRecord()) {
            role.preInsert();
            dao.insert(role);
        } else {
            role.preUpdate();
            dao.update(role);
        }

        dao.deleteRoleMenu(role);
        if (ListUtils.isNotEmpty(role.getMenuIdList())) {
            dao.insertRoleMenu(role);
        }

        signService.sign(role.getSignName(), role.getSignId(), role.getSignBody());
        notifyCacheChanged();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserList(Role role, List<User> userList) {
        dao.deleteRoleUser(role);

        if (ListUtils.isNotEmpty(userList)) {
            dao.insertRoleUser(role, userList);
        }

        signService.sign(role.getSignName(), role.getSignId(), role.getSignBody());
        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(Role role) {
        role.preUpdate();

        int result = dao.updateEnableFlag(role);

        signService.sign(role.getSignName(), role.getSignId(), role.getSignBody());
        notifyCacheChanged();

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
        int retVal = dao.delete(role);

        signService.deleteSign(role.getSignName(), role.getSignId());
        notifyCacheChanged();

        return retVal;
    }

    @Override
    public List<User> findRoleUser(Role role) {
        List<String> userIdList = idUserIdsMapHandler.computeIfAbsent(MapUtils::newHashMap)
                .computeIfAbsent(role.getId(), roleId -> ListUtils.map(dao.findRoleUser(role), User::getId));

        return ListUtils.map(userIdList, User::new);
    }

    @Override
    public List<Menu> findRoleMenu(Role role) {
        List<String> menuIdList = idMenuIdsMapHandler.computeIfAbsent(MapUtils::newHashMap)
                .computeIfAbsent(String.valueOf(role.getId()), roleId -> ListUtils.map(dao.findRoleMenu(role), Menu::getId));

        return ListUtils.map(menuIdList, Menu::new);
    }

    private void notifyCacheChanged() {
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
