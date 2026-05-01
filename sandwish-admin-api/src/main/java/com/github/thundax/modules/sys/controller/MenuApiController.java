package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.MoveTreeNodeException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.MenuInterfaceAssembler;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.request.MenuDisplayRequest;
import com.github.thundax.modules.sys.request.MenuIdRequest;
import com.github.thundax.modules.sys.request.MenuMoveRequest;
import com.github.thundax.modules.sys.request.MenuQueryRequest;
import com.github.thundax.modules.sys.request.MenuSaveRequest;
import com.github.thundax.modules.sys.response.MenuResponse;
import com.github.thundax.modules.sys.service.MenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "02-03.系统-菜单")
@SysLogger(module = {"系统", "菜单"})
@RequestMapping(value = "/api/sys/menu")
@RestController
public class MenuApiController extends BaseApiController {

    private final MenuService menuService;

    @Autowired
    public MenuApiController(MenuService menuService, Validator validator) {
        super(validator);
        this.menuService = menuService;
    }

    @ApiOperation(value = "获取对象", notes = "super")
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
    public MenuResponse get(@RequestBody MenuIdRequest request) throws ApiException {
        Menu bean = menuService.get(MenuInterfaceAssembler.toEntityId(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, request.getId());
        }
        return MenuInterfaceAssembler.toResponse(bean);
    }

    @ApiOperation(value = "获取列表", notes = "super")
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
    public List<MenuResponse> list(@RequestBody MenuQueryRequest request) throws ApiException {
        validate(request);

        Menu query = new Menu();
        Menu.Query queryCondition = new Menu.Query();

        queryCondition.setParentId(request.getParentId());
        if (request.getDisplay() != null) {
            queryCondition.setVisibility(request.getDisplay() ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN);
        }
        query.setQuery(queryCondition);

        return menuService.findList(query).stream()
                .map(menu -> MenuInterfaceAssembler.toResponse(menu))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "添加", notes = "super")
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
    public MenuResponse add(@RequestBody MenuSaveRequest request) throws ApiException {
        validate(request);

        Menu entity = MenuInterfaceAssembler.toEntity(new Menu(), request);
        if (entity.getId() != null) {
            Menu bean = menuService.get(MenuInterfaceAssembler.toEntityId(EntityIdCodec.toValue(entity.getId())));
            if (bean != null) {
                throw new InsertBeanExistException(Menu.BEAN_NAME, EntityIdCodec.toValue(entity.getId()));
            }
        }

        if (StringUtils.isNotEmpty(entity.getParentId())) {
            Menu parent = menuService.get(MenuInterfaceAssembler.toEntityId(entity.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        menuService.add(entity);

        return MenuInterfaceAssembler.toResponse(entity);
    }

    @ApiOperation(value = "更新", notes = "super")
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
    public MenuResponse update(@RequestBody MenuSaveRequest request) throws ApiException {
        validate(request);

        Menu bean = menuService.get(MenuInterfaceAssembler.toEntityId(request.getId()));
        if (bean == null) {
            throw new InvalidParameterException("id");
        }

        if (StringUtils.isNotEmpty(request.getParentId())) {
            Menu parent = menuService.get(MenuInterfaceAssembler.toEntityId(request.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        Menu entity = MenuInterfaceAssembler.toEntity(bean, request);

        menuService.update(entity);

        return MenuInterfaceAssembler.toResponse(entity);
    }

    @ApiOperation(value = "显示/隐藏", notes = "super")
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
    public Boolean updateVisibility(@RequestBody List<MenuDisplayRequest> list) throws ApiException {
        List<Menu> beanList = validateList(
                list,
                vo -> menuService.get(MenuInterfaceAssembler.toEntityId(vo.getId())),
                null,
                (bean, vo) -> bean.setVisibility(
                        Boolean.TRUE.equals(vo.getDisplay()) ? MenuVisibility.VISIBLE : MenuVisibility.HIDDEN));

        menuService.updateVisibility(beanList);

        return true;
    }

    @ApiOperation(value = "删除", notes = "super")
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
    public Boolean delete(@RequestBody List<MenuIdRequest> list) throws ApiException {
        List<Menu> beanList =
                validateList(list, vo -> menuService.get(MenuInterfaceAssembler.toEntityId(vo.getId())), null, null);

        menuService.delete(beanList);

        return true;
    }

    @ApiOperation(value = "获取列表", notes = "super")
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
    public List<MenuResponse> tree(@RequestBody List<MenuIdRequest> excludeList) {
        List<Menu> beanList = menuService.findList(new Menu());

        Set<String> excludeIds = excludeList == null
                ? new HashSet<>()
                : new HashSet<>(
                        excludeList.stream().map(request -> request.getId()).collect(Collectors.toList()));
        beanList.removeIf(bean -> excludeIds.contains(EntityIdCodec.toValue(bean.getId())));

        removeTreeNode(
                beanList,
                new RemoveTreeNodeSupport<Menu>() {

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
    public Boolean move(@RequestBody MenuMoveRequest request) throws ApiException {
        validate(request);

        Menu fromBean = menuService.get(MenuInterfaceAssembler.toEntityId(request.getFromNodeId()));
        if (fromBean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, request.getFromNodeId());
        }

        Menu toBean = menuService.get(MenuInterfaceAssembler.toEntityId(request.getToNodeId()));
        if (toBean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, request.getToNodeId());
        }

        if (toBean.equals(fromBean) || menuService.isChildOf(toBean, fromBean)) {
            throw new MoveTreeNodeException(Menu.BEAN_NAME, request.getFromNodeId(), request.getToNodeId());
        }

        menuService.moveTreeNode(fromBean, toBean, readMoveTreeNodeType(request));

        return true;
    }

    private TreeService.MoveTreeNodeType readMoveTreeNodeType(MenuMoveRequest request) {
        switch (request.getType()) {
            case MenuMoveRequest.TYPE_BEFORE:
                return TreeService.MoveTreeNodeType.BEFORE;
            case MenuMoveRequest.TYPE_INSIDE:
                return TreeService.MoveTreeNodeType.INSIDE;
            case MenuMoveRequest.TYPE_INSIDE_LAST:
                return TreeService.MoveTreeNodeType.INSIDE_LAST;
            default:
                return TreeService.MoveTreeNodeType.AFTER;
        }
    }
}
