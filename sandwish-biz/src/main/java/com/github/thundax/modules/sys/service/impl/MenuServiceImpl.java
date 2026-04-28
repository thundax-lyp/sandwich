package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.service.MenuService;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService {

    private final MenuDao dao;
    private final SignService signService;

    @Autowired
    public MenuServiceImpl(MenuDao dao, SignService signService) {
        this.dao = dao;
        this.signService = signService;
    }

    @Override
    public Class<Menu> getElementType() {
        return Menu.class;
    }

    @Override
    public Menu newEntity(String id) {
        return new Menu(id);
    }

    @Override
    public Menu get(Menu entity) {
        return entity == null ? null : get(entity.getId());
    }

    @Override
    public Menu get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return dao.get(id);
    }

    @Override
    public List<Menu> getMany(List<String> ids) {
        return dao.getMany(ids);
    }

    @Override
    public List<Menu> findList(Menu menu) {
        Menu.Query query = menu == null ? null : menu.getQuery();
        return dao.findList(
                query == null ? null : query.getParentId(),
                query == null ? null : query.getDisplayFlag(),
                query == null ? null : query.getMaxRank());
    }

    @Override
    public Menu findOne(Menu menu) {
        List<Menu> menus = findList(menu);
        return menus == null || menus.isEmpty() ? null : menus.get(0);
    }

    @Override
    public Page<Menu> findPage(Menu menu, Page<Menu> page) {
        Page<Menu> normalizedPage = normalizePage(page);
        Menu.Query query = menu == null ? null : menu.getQuery();
        IPage<Menu> dataPage = dao.findPage(
                query == null ? null : query.getParentId(),
                query == null ? null : query.getDisplayFlag(),
                query == null ? null : query.getMaxRank(),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    public long count(Menu menu) {
        return findList(menu).size();
    }

    @Override
    public List<Menu> findList(Integer maxRank) {
        return dao.findList(null, null, maxRank);
    }

    @Override
    public List<Menu> findChildList(String parentId) {
        return dao.findList(parentId, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Menu menu) {
        menu.preInsert();
        menu.setId(dao.insert(menu));
        afterWrite(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Menu menu) {
        menu.preUpdate();
        dao.update(menu);
        afterWrite(menu);
    }

    private void afterWrite(Menu menu) {
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
    public int updatePriority(Menu menu) {
        menu.preUpdate();
        return dao.updatePriority(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<Menu> list) {
        return batchOperate(list, this::updatePriority);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDelFlag(Menu menu) {
        menu.preUpdate();
        return dao.updateDelFlag(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDelFlag(List<Menu> list) {
        return batchOperate(list, this::updateDelFlag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Menu menu) {
        dao.deleteMenuRole(menu.getId());
        Menu bean = this.get(menu.getId());
        if (bean == null) {
            return 0;
        }

        int retVal = dao.delete(bean.getId());

        signService.deleteSign(menu.getSignName(), menu.getSignId());
        notifyCacheChanged();

        return retVal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(List<Menu> list) {
        return batchOperate(list, this::delete);
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
        try {
            SpringContextHolder.getBeansOfType(CacheChangedListener.class)
                    .forEach((name, listener) -> listener.onMenuCacheChanged());
        } catch (IllegalStateException | NullPointerException ignored) {
            // Unit tests may instantiate the service without a Spring application context.
        }
    }

    public interface CacheChangedListener {

        void onMenuCacheChanged();
    }

    private int batchOperate(Collection<Menu> collection, Function<Menu, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (Menu menu : collection) {
                count += operator.apply(menu);
            }
        }
        return count;
    }

    private Page<Menu> normalizePage(Page<Menu> page) {
        Page<Menu> normalizedPage = page == null ? new Page<>() : page;
        if (normalizedPage.getPageNo() < Page.FIRST_PAGE_INDEX) {
            normalizedPage.setPageNo(Page.FIRST_PAGE_INDEX);
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(Page.DEFAULT_PAGE_SIZE);
        }
        return normalizedPage;
    }
}
