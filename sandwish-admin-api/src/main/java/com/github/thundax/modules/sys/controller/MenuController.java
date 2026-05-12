package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.exception.AdminResponseExceptions;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.token.AccessTokenNames;
import com.github.thundax.common.tree.TreeNodeListHelper;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.request.RequestListHelper;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.MenuInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.MenuDisplayRequest;
import com.github.thundax.modules.sys.controller.request.MenuIdRequest;
import com.github.thundax.modules.sys.controller.request.MenuMoveRequest;
import com.github.thundax.modules.sys.controller.request.MenuQueryRequest;
import com.github.thundax.modules.sys.controller.request.MenuSaveRequest;
import com.github.thundax.modules.sys.controller.response.MenuResponse;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.MenuId;
import com.github.thundax.modules.sys.entity.valueobject.MenuIdCodec;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.command.ChangeMenuVisibilityCommand;
import com.github.thundax.modules.sys.service.command.DeleteMenuCommand;
import com.github.thundax.modules.sys.service.command.MoveMenuCommand;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "系统/菜单")
@SysLogger(module = {"系统", "菜单"})
@RequestMapping(value = "/api/sys/menu")
@WrappedApiController
public class MenuController {

    private static final String MENU_NAME = "Menu";

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @ApiOperation(value = "获取对象", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("super")
    @SysLogger("读取")
    @PostMapping(value = "get")
    public MenuResponse get(@Valid @RequestBody MenuIdRequest request) {
        Menu bean = menuService.get(MenuIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        return MenuInterfaceAssembler.toResponse(bean);
    }

    @ApiOperation(value = "获取列表", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("super")
    @SysLogger("读取")
    @PostMapping(value = "list")
    public List<MenuResponse> list(@Valid @RequestBody MenuQueryRequest request) {
        MenuQuery query = MenuInterfaceAssembler.toQuery(request);

        return menuService.list(query).stream()
                .map(menu -> MenuInterfaceAssembler.toResponse(menu))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "添加", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("super")
    @SysLogger("添加")
    @PostMapping(value = "create")
    public MenuResponse add(@Valid @RequestBody MenuSaveRequest request) {
        Menu entity = MenuInterfaceAssembler.toEntity(new Menu(), request);
        if (entity.getId() != null) {
            Menu bean = menuService.get(entity.getId());
            if (bean != null) {
                throw AdminResponseExceptions.objectExists();
            }
        }

        if (entity.getParentId() != null) {
            Menu parent = menuService.get(entity.getParentId());
            if (parent == null) {
                throw AdminResponseExceptions.invalidParameter("parentId");
            }
        }

        entity.setId(menuService.create(MenuInterfaceAssembler.toCreateCommand(request)));

        return MenuInterfaceAssembler.toResponse(entity);
    }

    @ApiOperation(value = "更新", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("super")
    @SysLogger("修改")
    @PostMapping(value = "update")
    public MenuResponse update(@Valid @RequestBody MenuSaveRequest request) {
        Menu bean = menuService.get(MenuIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.invalidParameter("id");
        }

        if (request.getParentId() != null) {
            Menu parent = menuService.get(MenuIdCodec.toDomain(request.getParentId()));
            if (parent == null) {
                throw AdminResponseExceptions.invalidParameter("parentId");
            }
        }

        Menu entity = MenuInterfaceAssembler.toEntity(bean, request);

        menuService.changeInfo(MenuInterfaceAssembler.toChangeInfoCommand(request));

        return MenuInterfaceAssembler.toResponse(entity);
    }

    @ApiOperation(value = "显示/隐藏", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("super")
    @SysLogger("显示")
    @PostMapping(value = "display")
    public Boolean updateVisibility(@Valid @RequestBody List<MenuDisplayRequest> list) {
        List<ChangeMenuVisibilityCommand> commandList = new ArrayList<>();
        for (MenuDisplayRequest request : RequestListHelper.present(list)) {
            Menu bean = menuService.get(MenuIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            commandList.add(new ChangeMenuVisibilityCommand(
                    bean.getId(),
                    Boolean.TRUE.equals(request.getDisplay()) ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN));
        }
        if (commandList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        commandList.forEach(menuService::changeVisibility);

        return true;
    }

    @ApiOperation(value = "删除", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("super")
    @SysLogger("删除")
    @PostMapping(value = "delete")
    public Boolean delete(@Valid @RequestBody List<MenuIdRequest> list) {
        List<DeleteMenuCommand> commandList = new ArrayList<>();
        for (MenuIdRequest request : RequestListHelper.present(list)) {
            Menu bean = menuService.get(MenuIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            commandList.add(new DeleteMenuCommand(bean.getId()));
        }
        if (commandList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        commandList.forEach(menuService::remove);

        return true;
    }

    @ApiOperation(value = "获取列表", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("super")
    @SysLogger("读取")
    @PostMapping(value = "tree")
    public List<MenuResponse> tree(@Valid @RequestBody List<MenuIdRequest> excludeList) {
        List<Menu> beanList = menuService.list(new MenuQuery());

        Set<MenuId> excludeIds =
                new HashSet<>(RequestListHelper.map(excludeList, request -> MenuIdCodec.toDomain(request.getId())));
        beanList.removeIf(bean -> excludeIds.contains(bean.getId()));

        TreeNodeListHelper.remove(
                beanList,
                new TreeNodeListHelper.TreeNodeSupport<Menu, MenuId>() {

                    @Override
                    public MenuId getId(Menu menu) {
                        return menu.getId();
                    }

                    @Override
                    public MenuId getParentId(Menu menu) {
                        return menu.getParentId();
                    }

                    @Override
                    public boolean isRoot(Menu menu) {
                        return menu.getParentId() == null;
                    }
                },
                excludeIds);

        return beanList.stream()
                .map(menu -> MenuInterfaceAssembler.toTreeResponse(menu))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "排序", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("super")
    @SysLogger("排序")
    @PostMapping(value = "move")
    public Boolean move(@Valid @RequestBody MenuMoveRequest request) {
        Menu fromBean = menuService.get(MenuIdCodec.toDomain(request.getFromNodeId()));
        if (fromBean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }

        Menu toBean = menuService.get(MenuIdCodec.toDomain(request.getToNodeId()));
        if (toBean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }

        if (toBean.equals(fromBean) || menuService.existsChildRelation(childRelationQuery(toBean, fromBean))) {
            throw AdminResponseExceptions.moveTreeNode();
        }

        menuService.move(new MoveMenuCommand(fromBean.getId(), toBean.getId(), readMoveTreeNodeType(request)));

        return true;
    }

    private TreeNodeMoveType readMoveTreeNodeType(MenuMoveRequest request) {
        switch (request.getType()) {
            case MenuMoveRequest.TYPE_BEFORE:
                return TreeNodeMoveType.BEFORE;
            case MenuMoveRequest.TYPE_INSIDE:
                return TreeNodeMoveType.INSIDE;
            case MenuMoveRequest.TYPE_INSIDE_LAST:
                return TreeNodeMoveType.INSIDE_LAST;
            default:
                return TreeNodeMoveType.AFTER;
        }
    }

    private MenuQuery childRelationQuery(Menu child, Menu ancestor) {
        MenuQuery query = new MenuQuery();
        query.setChildId(child.getId());
        query.setAncestorId(ancestor.getId());
        return query;
    }
}
