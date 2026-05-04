package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.query.MenuQuery;
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

    public Menu getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<Menu> listByIds(List<EntityId> ids) {
        return dao.listByIds(EntityIdCodec.toValues(ids));
    }

    public List<Menu> list(Menu menu) {
        return list((MenuQuery) null);
    }

    public List<Menu> list(MenuQuery query) {
        return dao.list(
                query == null ? null : query.getParentId(),
                query == null ? null : visibilityValue(query.getVisibility()),
                query == null ? null : query.getMaxRank());
    }

    public PageDTO<Menu> page(MenuQuery query, PageDTO<Menu> page) {
        PageDTO<Menu> normalizedPage = normalizePage(page);
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

    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        dao.deleteMenuRole(EntityIdCodec.toValue(id));
        Menu bean = this.getById(id);
        if (bean == null) {
            return 0;
        }

        int retVal = dao.deleteById(bean.getId());

        signService.deleteSign(bean.getSignName(), bean.getSignId());
        notifyCacheChanged();

        return retVal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(Menu from, Menu to, TreeNodeMoveType moveType) {
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

    private <T> int batchOperate(Collection<T> collection, Function<T, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (T entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private PageDTO<Menu> normalizePage(PageDTO<Menu> page) {
        PageDTO<Menu> normalizedPage = page == null ? new PageDTO<>() : page;
        if (normalizedPage.getPageNo() < PageRules.firstPageIndex()) {
            normalizedPage.setPageNo(PageRules.firstPageIndex());
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(PageRules.defaultPageSize());
        }
        return normalizedPage;
    }

    private String visibilityValue(MenuVisibility visibility) {
        return visibility == null ? null : visibility.value();
    }
}
