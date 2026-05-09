package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.sys.codec.AccessRankCodec;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.command.ChangeMenuInfoCommand;
import com.github.thundax.modules.sys.service.command.ChangeMenuVisibilityCommand;
import com.github.thundax.modules.sys.service.command.CreateMenuCommand;
import com.github.thundax.modules.sys.service.command.DeleteMenuCommand;
import com.github.thundax.modules.sys.service.command.MoveMenuCommand;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService {

    private final MenuDao dao;

    @Autowired
    public MenuServiceImpl(MenuDao dao) {
        this.dao = dao;
    }

    public Menu get(MenuQuery query) {
        if (query == null || query.getId() == null) {
            return null;
        }
        return dao.getById(query.getId());
    }

    public List<Menu> list(MenuQuery query) {
        if (query != null && query.getIds() != null) {
            return dao.listByIds(EntityIdCodec.toValues(query.getIds()));
        }
        return dao.list(
                query == null ? null : query.getParentId(),
                query == null ? null : visibilityValue(query.getVisibility()),
                query == null ? null : rankValue(query.getMaxRank()));
    }

    public PageResult<Menu> page(MenuQuery query, PageQuery page) {
        PageQuery normalizedPage = normalizePage(page);
        IPage<Menu> dataPage = dao.page(
                query == null ? null : query.getParentId(),
                query == null ? null : visibilityValue(query.getVisibility()),
                query == null ? null : rankValue(query.getMaxRank()),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(),
                (int) dataPage.getSize(),
                dataPage.getTotal(),
                dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityId create(CreateMenuCommand command) {
        Menu menu = toMenu(command);
        menu.setId(dao.insert(menu));
        afterWrite(menu);
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInfo(ChangeMenuInfoCommand command) {
        Menu menu = toMenu(command);
        dao.update(menu);
        afterWrite(menu);
    }

    private void afterWrite(Menu menu) {
        notifyCacheChanged();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeVisibility(ChangeMenuVisibilityCommand command) {
        Menu menu = new Menu();
        menu.setId(command.getId());
        menu.setVisibility(command.getVisibility());
        int result = dao.updateVisibility(menu);
        notifyCacheChanged();
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public int remove(DeleteMenuCommand command) {
        dao.deleteMenuRole(EntityIdCodec.toValue(command.getId()));
        MenuQuery query = new MenuQuery();
        query.setId(command.getId());
        Menu bean = this.get(query);
        if (bean == null) {
            return 0;
        }

        int retVal = dao.deleteById(bean.getId());

        notifyCacheChanged();

        return retVal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(MoveMenuCommand command) {
        dao.moveTreeNode(
                EntityIdCodec.toValue(command.getFromId()),
                EntityIdCodec.toValue(command.getToId()),
                command.getMoveType());
        notifyCacheChanged();
    }

    @Override
    public boolean existsChildRelation(MenuQuery query) {
        return query != null
                && query.getChildId() != null
                && query.getAncestorId() != null
                && dao.isChildOf(
                        EntityIdCodec.toValue(query.getChildId()), EntityIdCodec.toValue(query.getAncestorId()));
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

    private PageQuery normalizePage(PageQuery page) {
        PageQuery normalizedPage = page == null ? new PageQuery() : page;
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

    private Integer rankValue(AccessRank rank) {
        return rank == null ? null : AccessRankCodec.toValue(rank);
    }

    private Menu toMenu(CreateMenuCommand command) {
        Menu menu = new Menu();
        menu.setId(command.getId());
        menu.setParentId(command.getParentId());
        menu.setName(command.getName());
        menu.setPerms(command.getPerms());
        menu.setRank(command.getRank());
        menu.setVisibility(command.getVisibility());
        menu.setDisplayParams(command.getDisplayParams());
        menu.setUrl(command.getUrl());
        menu.setTarget(command.getTarget());
        menu.setPriority(command.getPriority());
        menu.setRemarks(command.getRemarks());
        return menu;
    }

    private Menu toMenu(ChangeMenuInfoCommand command) {
        Menu menu = new Menu();
        menu.setId(command.getId());
        menu.setParentId(command.getParentId());
        menu.setName(command.getName());
        menu.setPerms(command.getPerms());
        menu.setRank(command.getRank());
        menu.setVisibility(command.getVisibility());
        menu.setDisplayParams(command.getDisplayParams());
        menu.setUrl(command.getUrl());
        menu.setTarget(command.getTarget());
        menu.setPriority(command.getPriority());
        menu.setRemarks(command.getRemarks());
        return menu;
    }
}
