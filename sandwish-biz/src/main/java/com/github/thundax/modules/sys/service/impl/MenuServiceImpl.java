package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.Constants;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.MapUtils;
import com.github.thundax.common.persistence.TreeEntity;
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

            Integer newPosition;
            if (StringUtils.isNotBlank(menu.getParentId())
                    && !StringUtils.equals(menu.getParentId(), TreeEntity.ROOT_ID)) {
                Menu parent = dao.getTreeNode(menu.getParentId());
                newPosition = parent.getRgt();
            } else {
                menu.setParentId(null);
                Integer maxRgt = dao.getMaxPosition();
                if (maxRgt == null) {
                    maxRgt = 0;
                }
                newPosition = maxRgt + 1;
            }
            menu.setLft(newPosition);
            menu.setRgt(newPosition + 1);
            dao.moveTreeRgts(newPosition, 2);
            dao.moveTreeLfts(newPosition, 2);
            dao.insert(menu);

        } else {
            menu.preUpdate();
            Menu oldBean = dao.getTreeNode(menu.getId());
            if (!StringUtils.equals(oldBean.getParentId(), menu.getParentId())) {
                Integer newPosition;
                if (StringUtils.isNotBlank(menu.getParentId())) {
                    Menu parent = dao.getTreeNode(menu.getParentId());
                    newPosition = parent.getRgt();
                } else {
                    newPosition = dao.getMaxPosition() + 1;
                }
                dao.moveTreeRgts(newPosition, oldBean.treeSpan());
                dao.moveTreeLfts(newPosition, oldBean.treeSpan());

                oldBean = dao.getTreeNode(menu.getId());
                int offset = newPosition - oldBean.getLft();
                dao.moveTreeNodes(oldBean.getLft(), oldBean.getRgt(), offset);

                dao.moveTreeRgts(oldBean.getLft(), -oldBean.treeSpan());
                dao.moveTreeLfts(oldBean.getLft(), -oldBean.treeSpan());
            }
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
        dao.moveTreeRgts(bean.getLft(), -bean.treeSpan());
        dao.moveTreeLfts(bean.getLft(), -bean.treeSpan());

        int retVal = dao.delete(bean);

        removeAllCache();
        signService.deleteSign(menu.getSignName(), menu.getSignId());

        return retVal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(Menu from, Menu to, MoveTreeNodeType moveType) {
        Menu fromBean = dao.getTreeNode(from.getId());
        Menu toBean = dao.getTreeNode(to.getId());

        int newPosition;
        String newParentId;
        if (moveType == MoveTreeNodeType.AFTER) {
            newPosition = toBean.getRgt() + 1;
            newParentId = toBean.getParentId();

        } else if (moveType == MoveTreeNodeType.BEFORE) {
            newPosition = toBean.getLft();
            newParentId = toBean.getParentId();

        } else if (moveType == MoveTreeNodeType.INSIDE) {
            newPosition = toBean.getLft() + 1;
            newParentId = to.getId();

        } else {
            newPosition = toBean.getRgt();
            newParentId = to.getId();
        }

        dao.moveTreeLfts(newPosition, fromBean.treeSpan());
        dao.moveTreeRgts(newPosition, fromBean.treeSpan());

        fromBean = dao.getTreeNode(from.getId());
        int offset = newPosition - fromBean.getLft();
        dao.moveTreeNodes(fromBean.getLft(), fromBean.getRgt(), offset);

        dao.moveTreeLfts(fromBean.getLft(), -fromBean.treeSpan());
        dao.moveTreeRgts(fromBean.getLft(), -fromBean.treeSpan());

        from.setParentId(newParentId);
        dao.updateParent(from);

        removeAllCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLftRgt(Menu entity) {
        dao.updateLftRgt(entity);
        removeAllCache();
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
