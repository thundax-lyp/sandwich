package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.Constants;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.MapUtils;
import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class MenuServiceImpl extends CrudServiceImpl<MenuDao, Menu> implements MenuService {

    private String lastCacheVersion;
    private final SignService signService;
    private final List<Menu> cacheMenuList = ListUtils.newArrayList();

    @Autowired
    public MenuServiceImpl(MenuDao dao, RedisClient redisClient, SignService signService) {
        super(dao, redisClient);
        this.signService = signService;
    }

    @Override
    protected boolean isRedisCacheEnabled() {
        return true;
    }

    @Override
    protected String getCacheSection() {
        return Constants.CACHE_PREFIX + "sys.menu.";
    }

    @Override
    public void initialize() {
        //此处没有加锁，先更新版本，后更新数据，保证数据比版本新
        lastCacheVersion = getCacheVersion();
        cacheMenuList.addAll(dao.findList(new Menu()));
    }

    private synchronized List<Menu> findAllList() {
        String currentCacheVersion = getCacheVersion();

        if (!StringUtils.equals(currentCacheVersion, lastCacheVersion)) {
            cacheMenuList.clear();

            lastCacheVersion = getCacheVersion();
            cacheMenuList.addAll(dao.findList(new Menu()));
        }

        return cacheMenuList;
    }


    @Override
    public Menu get(String id) {
        return ListUtils.find(findAllList(), item -> Objects.equals(item.getId(), id));
    }


    @Override
    public Menu get(Menu menu) {
        return get(menu.getId());
    }


    @Override
    public List<Menu> findList(Integer maxRank) {
        List<Menu> menus = ListUtils.newArrayList(findAllList());
        menus.removeIf(menu -> menu.getRanks() > maxRank);
        return menus;
    }


    @Override
    public List<Menu> findChildList(String parentId) {
        List<Menu> menus = ListUtils.newArrayList(findAllList());
        menus.removeIf(menu -> !StringUtils.equals(menu.getParentId(), parentId));
        return menus;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Menu menu) {
        if (menu.getIsNewRecord()) {
            menu.preInsert();
            dao.insert(menu);
        } else {
            menu.preUpdate();
            dao.update(menu);
        }

        removeAllCache();
        signService.sign(menu.getSignName(), menu.getSignId(), menu.getSignBody());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDisplayFlag(Menu menu) {
        menu.preUpdate();
        int result = dao.updateDisplayFlag(menu);
        removeCache(menu);
        signService.sign(menu.getSignName(), menu.getSignId(), menu.getSignBody());
        return result;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDisplayFlag(List<Menu> list) {
        return batchOperate(list, this::updateDisplayFlag);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Menu menu) {
        dao.deleteMenuRole(menu);
        Menu bean = this.get(menu.getId());
        if (bean == null) {
            return 0;
        }

        int retVal = dao.delete(bean);

        removeAllCache();
        signService.deleteSign(menu.getSignName(), menu.getSignId());

        return retVal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(Menu from, Menu to, MoveTreeNodeType moveType) {
        dao.moveTreeNode(from.getId(), to.getId(), moveType);
        removeAllCache();
    }

    @Override
    public boolean isChildOf(Menu child, Menu parent) {
        return child != null && parent != null && dao.isChildOf(child.getId(), parent.getId());
    }

    @Override
    protected void removeCache(Menu entity) {
        super.removeCache(entity);

        MapUtils.forEach(SpringContextHolder.getBeansOfType(CacheChangedListener.class),
                (name, listener) -> listener.onMenuCacheChanged());
    }

    public interface CacheChangedListener {
        /**
         * cache changed event
         */
        void onMenuCacheChanged();
    }
}
