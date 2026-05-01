package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.service.MenuService;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
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
        Menu menu = new Menu();
        menu.setId(EntityIdCodec.toDomain(id));
        return menu;
    }

    @Override
    public Menu getById(Menu entity) {
        return entity == null ? null : getById(entity.getId());
    }

    @Override
    public Menu getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<Menu> batchGetByIds(List<String> ids) {
        return dao.batchGetByIds(ids);
    }

    @Override
    public List<Menu> list(Menu menu) {
        Menu.Query query = menu == null ? null : menu.getQuery();
        return dao.list(
                query == null ? null : query.getParentId(),
                query == null ? null : visibilityValue(query.getVisibility()),
                query == null ? null : query.getMaxRank());
    }

    @Override
    public Menu getOne(Menu menu) {
        List<Menu> menus = list(menu);
        return menus == null || menus.isEmpty() ? null : menus.get(0);
    }

    @Override
    public Page<Menu> page(Menu menu, Page<Menu> page) {
        Page<Menu> normalizedPage = normalizePage(page);
        Menu.Query query = menu == null ? null : menu.getQuery();
        IPage<Menu> dataPage = dao.page(
                query == null ? null : query.getParentId(),
                query == null ? null : visibilityValue(query.getVisibility()),
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
        return list(menu).size();
    }

    @Override
    public List<Menu> list(Integer maxRank) {
        return dao.list(null, null, maxRank);
    }

    @Override
    public List<Menu> listChildren(String parentId) {
        return dao.list(parentId, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Menu menu) {
        menu.setId(EntityIdCodec.toDomain(dao.insert(menu)));
        afterWrite(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Menu menu) {
        dao.update(menu);
        afterWrite(menu);
    }

    private void afterWrite(Menu menu) {
        signService.sign(menu.getSignName(), menu.getSignId(), menu.getSignBody());
        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateVisibility(Menu menu) {
        int result = dao.updateVisibility(menu);
        signService.sign(menu.getSignName(), menu.getSignId(), menu.getSignBody());
        notifyCacheChanged();
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateVisibility(List<Menu> list) {
        return batchOperate(list, this::updateVisibility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(Menu menu) {
        return dao.updatePriority(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<Menu> list) {
        return batchOperate(list, this::updatePriority);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Menu menu) {
        dao.deleteMenuRole(EntityIdCodec.toValue(menu.getId()));
        Menu bean = this.getById(menu.getId());
        if (bean == null) {
            return 0;
        }

        int retVal = dao.deleteById(bean.getId());

        signService.deleteSign(menu.getSignName(), menu.getSignId());
        notifyCacheChanged();

        return retVal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<Menu> list) {
        return batchOperate(list, this::deleteById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(Menu from, Menu to, MoveTreeNodeType moveType) {
        dao.moveTreeNode(EntityIdCodec.toValue(from.getId()), EntityIdCodec.toValue(to.getId()), moveType);
        notifyCacheChanged();
    }

    @Override
    public boolean isChildOf(Menu child, Menu parent) {
        return child != null
                && parent != null
                && dao.isChildOf(EntityIdCodec.toValue(child.getId()), EntityIdCodec.toValue(parent.getId()));
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

    private String visibilityValue(MenuVisibility visibility) {
        return visibility == null ? null : visibility.value();
    }
}
