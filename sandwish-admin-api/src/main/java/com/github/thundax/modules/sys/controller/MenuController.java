package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.collection.TreeNodeListHelper;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.MoveTreeNodeException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.security.annotation.HasPermission;
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
import com.github.thundax.modules.sys.service.MenuService;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "系统/菜单")
@SysLogger(module = {"系统", "菜单"})
@RequestMapping(value = "/api/sys/menu")
@WrappedApiController
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @ApiOperation(value = "获取对象", notes = "super")
    @HasPermission("super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public MenuResponse get(@Valid @RequestBody MenuIdRequest request) throws ApiException {
        Menu bean = menuService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, request.getId());
        }
        return MenuInterfaceAssembler.toResponse(bean);
    }

    @ApiOperation(value = "获取列表", notes = "super")
    @HasPermission("super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public List<MenuResponse> list(@Valid @RequestBody MenuQueryRequest request) throws ApiException {
        MenuQuery query = MenuInterfaceAssembler.toQuery(request);

        return menuService.list(query).stream()
                .map(menu -> MenuInterfaceAssembler.toResponse(menu))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "添加", notes = "super")
    @HasPermission("super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public MenuResponse add(@Valid @RequestBody MenuSaveRequest request) throws ApiException {
        Menu entity = MenuInterfaceAssembler.toEntity(new Menu(), request);
        if (entity.getId() != null) {
            Menu bean = menuService.getById(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(Menu.BEAN_NAME, EntityIdCodec.toValue(entity.getId()));
            }
        }

        if (StringUtils.isNotEmpty(entity.getParentId())) {
            Menu parent = menuService.getById(EntityIdCodec.toDomain(entity.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        menuService.add(entity);

        return MenuInterfaceAssembler.toResponse(entity);
    }

    @ApiOperation(value = "更新", notes = "super")
    @HasPermission("super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("修改")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public MenuResponse update(@Valid @RequestBody MenuSaveRequest request) throws ApiException {
        Menu bean = menuService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new InvalidParameterException("id");
        }

        if (StringUtils.isNotEmpty(request.getParentId())) {
            Menu parent = menuService.getById(EntityIdCodec.toDomain(request.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        Menu entity = MenuInterfaceAssembler.toEntity(bean, request);

        menuService.update(entity);

        return MenuInterfaceAssembler.toResponse(entity);
    }

    @ApiOperation(value = "显示/隐藏", notes = "super")
    @HasPermission("super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("显示")
    @RequestMapping(value = "display", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public Boolean updateVisibility(@Valid @RequestBody List<MenuDisplayRequest> list) throws ApiException {
        List<Menu> beanList = new ArrayList<>();
        for (MenuDisplayRequest request : RequestListHelper.present(list)) {
            Menu bean = menuService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Menu.BEAN_NAME, request.getId());
            }
            bean.setVisibility(
                    Boolean.TRUE.equals(request.getDisplay()) ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN);
            beanList.add(bean);
        }
        if (beanList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        menuService.batchUpdateVisibility(beanList);

        return true;
    }

    @ApiOperation(value = "删除", notes = "super")
    @HasPermission("super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public Boolean delete(@Valid @RequestBody List<MenuIdRequest> list) throws ApiException {
        List<Menu> beanList = new ArrayList<>();
        for (MenuIdRequest request : RequestListHelper.present(list)) {
            Menu bean = menuService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Menu.BEAN_NAME, request.getId());
            }
            beanList.add(bean);
        }
        if (beanList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        menuService.batchDeleteById(beanList.stream().map(Menu::getId).collect(Collectors.toList()));

        return true;
    }

    @ApiOperation(value = "获取列表", notes = "super")
    @HasPermission("super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "tree", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public List<MenuResponse> tree(@Valid @RequestBody List<MenuIdRequest> excludeList) {
        List<Menu> beanList = menuService.list(new MenuQuery());

        Set<String> excludeIds = new HashSet<>(RequestListHelper.map(excludeList, MenuIdRequest::getId));
        beanList.removeIf(bean -> excludeIds.contains(EntityIdCodec.toValue(bean.getId())));

        TreeNodeListHelper.remove(
                beanList,
                new TreeNodeListHelper.TreeNodeSupport<Menu>() {

                    @Override
                    public String getId(Menu menu) {
                        return EntityIdCodec.toValue(menu.getId());
                    }

                    @Override
                    public String getParentId(Menu menu) {
                        return menu.getParentId();
                    }

                    @Override
                    public boolean isRoot(Menu menu) {
                        return StringUtils.isBlank(menu.getParentId());
                    }
                },
                excludeIds);

        return beanList.stream()
                .map(menu -> MenuInterfaceAssembler.toTreeResponse(menu))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "排序", notes = "super")
    @HasPermission("super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("排序")
    @RequestMapping(value = "move", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public Boolean move(@Valid @RequestBody MenuMoveRequest request) throws ApiException {
        Menu fromBean = menuService.getById(EntityIdCodec.toDomain(request.getFromNodeId()));
        if (fromBean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, request.getFromNodeId());
        }

        Menu toBean = menuService.getById(EntityIdCodec.toDomain(request.getToNodeId()));
        if (toBean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, request.getToNodeId());
        }

        if (toBean.equals(fromBean) || menuService.isChildOf(toBean, fromBean)) {
            throw new MoveTreeNodeException(Menu.BEAN_NAME, request.getFromNodeId(), request.getToNodeId());
        }

        menuService.moveTreeNode(fromBean, toBean, readMoveTreeNodeType(request));

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
}
