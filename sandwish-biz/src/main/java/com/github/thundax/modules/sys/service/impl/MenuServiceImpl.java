package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.service.MenuService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** @author wdit */
@Service
@Transactional(readOnly = true)
public class MenuServiceImpl extends CrudServiceImpl<MenuDao, Menu> implements MenuService {

    private final SignService signService;

    @Autowired
    public MenuServiceImpl(MenuDao dao, SignService signService) {
        super(dao);
        this.signService = signService;
    }

    @Override
    public List<Menu> findList(Integer maxRank) {
        List<Menu> menus = new ArrayList<>(dao.findList(new Menu()));
        menus.removeIf(menu -> menu.getRanks() > maxRank);
        return menus;
    }

    @Override
    public List<Menu> findChildList(String parentId) {
        List<Menu> menus = new ArrayList<>(dao.findList(new Menu()));
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

        signService.sign(menu.getSignName(), menu.getSignId(), menu.getSignBody());
        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDisplayFlag(Menu menu) {
        menu.preUpdate();
        int result = dao.updateDisplayFlag(menu);
        signService.sign(menu.getSignName(), menu.getSignId(), menu.getSignBody());
        notifyCacheChanged();
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

        signService.deleteSign(menu.getSignName(), menu.getSignId());
        notifyCacheChanged();

        return retVal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(Menu from, Menu to, MoveTreeNodeType moveType) {
        dao.moveTreeNode(from.getId(), to.getId(), moveType);
        notifyCacheChanged();
    }

    @Override
    public boolean isChildOf(Menu child, Menu parent) {
        return child != null && parent != null && dao.isChildOf(child.getId(), parent.getId());
    }

    private void notifyCacheChanged() {
        SpringContextHolder.getBeansOfType(CacheChangedListener.class)
                .forEach((name, listener) -> listener.onMenuCacheChanged());
    }

    public interface CacheChangedListener {
        /** cache changed event */
        void onMenuCacheChanged();
    }
}
