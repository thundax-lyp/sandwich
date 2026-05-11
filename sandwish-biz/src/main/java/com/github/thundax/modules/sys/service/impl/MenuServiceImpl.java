package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.audit.annotation.AuditLog;
import com.github.thundax.modules.audit.entity.enums.AuditAction;
import com.github.thundax.modules.exception.BizExceptionBoundary;
import com.github.thundax.modules.sys.codec.AccessRankCodec;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.entity.valueobject.MenuId;
import com.github.thundax.modules.sys.entity.valueobject.MenuIdCodec;
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
@BizExceptionBoundary
public class MenuServiceImpl implements MenuService {

    private final MenuDao dao;

    @Autowired
    public MenuServiceImpl(MenuDao dao) {
        this.dao = dao;
    }

    public Menu get(MenuId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<Menu> list(MenuQuery query) {
        if (query != null && query.getIds() != null) {
            return dao.listByIds(MenuIdCodec.toValues(query.getIds()));
        }
        return dao.list(
                query == null ? null : MenuIdCodec.toValue(query.getParentId()),
                query == null ? null : visibilityValue(query.getVisibility()),
                query == null ? null : rankValue(query.getMaxRank()));
    }

    public PageResult<Menu> page(MenuQuery query, PageQuery page) {
        PageQuery normalizedPage = normalizePage(page);
        IPage<Menu> dataPage = dao.page(
                query == null ? null : MenuIdCodec.toValue(query.getParentId()),
                query == null ? null : visibilityValue(query.getVisibility()),
                query == null ? null : rankValue(query.getMaxRank()),
                normalizedPage.getPageNo(),
                normalizedPage.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @AuditLog(type = "Menu", id = "", action = AuditAction.CREATE, summary = "创建菜单", recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public MenuId create(CreateMenuCommand command) {
        Menu menu = toMenu(command);
        menu.setId(dao.insert(menu));
        afterWrite(menu);
        return menu.getId();
    }

    @Override
    @AuditLog(type = "Menu", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "更新菜单")
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
    @AuditLog(type = "Menu", id = "#command.id.value()", action = AuditAction.UPDATE, summary = "更新菜单可见性")
    @Transactional(rollbackFor = Exception.class)
    public int changeVisibility(ChangeMenuVisibilityCommand command) {
        Menu menu = new Menu();
        menu.setId(command.getId());
        menu.setVisibility(command.getVisibility());
        int result = dao.updateVisibility(menu);
        notifyCacheChanged();
        return result;
    }

    @AuditLog(
            type = "Menu",
            id = "#command.id.value()",
            action = AuditAction.DELETE,
            summary = "删除菜单",
            recordWhenUnchanged = true)
    @Transactional(rollbackFor = Exception.class)
    public int remove(DeleteMenuCommand command) {
        dao.deleteMenuRole(MenuIdCodec.toValue(command.getId()));
        Menu bean = this.get(command.getId());
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
                MenuIdCodec.toValue(command.getFromId()),
                MenuIdCodec.toValue(command.getToId()),
                command.getMoveType());
        notifyCacheChanged();
    }

    @Override
    public boolean existsChildRelation(MenuQuery query) {
        return query != null
                && query.getChildId() != null
                && query.getAncestorId() != null
                && dao.isChildOf(MenuIdCodec.toValue(query.getChildId()), MenuIdCodec.toValue(query.getAncestorId()));
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
        menu.setRemarks(command.getRemarks());
        return menu;
    }
}
