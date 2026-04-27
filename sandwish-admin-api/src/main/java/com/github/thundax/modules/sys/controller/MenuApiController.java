package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.MoveTreeNodeException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.sys.api.MenuServiceApi;
import com.github.thundax.modules.sys.assembler.MenuInterfaceAssembler;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.request.MenuDisplayRequest;
import com.github.thundax.modules.sys.request.MenuIdRequest;
import com.github.thundax.modules.sys.request.MenuMoveRequest;
import com.github.thundax.modules.sys.request.MenuQueryRequest;
import com.github.thundax.modules.sys.request.MenuSaveRequest;
import com.github.thundax.modules.sys.response.MenuResponse;
import com.github.thundax.modules.sys.service.MenuService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** @author thundax */
@RestController
public class MenuApiController extends BaseApiController implements MenuServiceApi {

    private final MenuService menuService;
    private final MenuInterfaceAssembler menuInterfaceAssembler;

    @Autowired
    public MenuApiController(
            MenuService menuService,
            Validator validator,
            MenuInterfaceAssembler menuInterfaceAssembler) {
        super(validator);
        this.menuService = menuService;
        this.menuInterfaceAssembler = menuInterfaceAssembler;
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public MenuResponse get(@RequestBody MenuIdRequest request) throws ApiException {
        Menu bean = menuService.get(request.getId());
        if (bean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, request.getId());
        }
        return menuInterfaceAssembler.toResponse(bean);
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public List<MenuResponse> list(@RequestBody MenuQueryRequest request) throws ApiException {
        validate(request);

        Menu query = new Menu();
        Menu.Query queryCondition = new Menu.Query();

        queryCondition.setParentId(request.getParentId());
        if (request.getDisplay() != null) {
            queryCondition.setDisplayFlag(request.getDisplay() ? Global.SHOW : Global.HIDE);
        }
        query.setQuery(queryCondition);

        return ListUtils.map(menuService.findList(query), menuInterfaceAssembler::toResponse);
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public MenuResponse add(@RequestBody MenuSaveRequest request) throws ApiException {
        validate(request);

        Menu entity = menuInterfaceAssembler.toEntity(new Menu(), request);
        if (StringUtils.isNotEmpty(entity.getId())) {
            Menu bean = menuService.get(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(Menu.BEAN_NAME, entity.getId());
            }
            entity.setIsNewRecord(true);
        }

        if (StringUtils.isNotEmpty(entity.getParentId())) {
            Menu parent = menuService.get(entity.getParentId());
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        menuService.save(entity);

        return menuInterfaceAssembler.toResponse(entity);
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public MenuResponse update(@RequestBody MenuSaveRequest request) throws ApiException {
        validate(request);

        Menu bean = menuService.get(request.getId());
        if (bean == null) {
            throw new InvalidParameterException("id");
        }

        if (StringUtils.isNotEmpty(request.getParentId())) {
            Menu parent = menuService.get(request.getParentId());
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        Menu entity = menuInterfaceAssembler.toEntity(bean, request);

        menuService.save(entity);

        return menuInterfaceAssembler.toResponse(entity);
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public Boolean updateDisplayFlag(@RequestBody List<MenuDisplayRequest> list)
            throws ApiException {
        List<Menu> beanList =
                validateList(
                        list,
                        vo -> menuService.get(vo.getId()),
                        null,
                        (bean, vo) ->
                                bean.setDisplayFlag(
                                        Boolean.TRUE.equals(vo.getDisplay())
                                                ? Global.SHOW
                                                : Global.HIDE));

        menuService.updateDisplayFlag(beanList);

        return true;
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public Boolean delete(@RequestBody List<MenuIdRequest> list) throws ApiException {
        List<Menu> beanList = validateList(list, vo -> menuService.get(vo.getId()), null, null);

        menuService.delete(beanList);

        return true;
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public List<MenuResponse> tree(@RequestBody List<MenuIdRequest> excludeList) {
        List<Menu> beanList = menuService.findList(new Menu());

        Set<String> excludeIds = new HashSet<>(ListUtils.map(excludeList, MenuIdRequest::getId));
        beanList.removeIf(bean -> excludeIds.contains(bean.getId()));

        removeTreeNode(
                beanList,
                new RemoveTreeNodeSupport<Menu>() {

                    @Override
                    public String getId(Menu menu) {
                        return menu.getId();
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

        return ListUtils.map(beanList, menuInterfaceAssembler::toTreeResponse);
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('super')")
    public Boolean move(@RequestBody MenuMoveRequest request) throws ApiException {
        validate(request);

        Menu fromBean = menuService.get(request.getFromNodeId());
        if (fromBean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, request.getFromNodeId());
        }

        Menu toBean = menuService.get(request.getToNodeId());
        if (toBean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, request.getToNodeId());
        }

        if (toBean.equals(fromBean) || menuService.isChildOf(toBean, fromBean)) {
            throw new MoveTreeNodeException(
                    Menu.BEAN_NAME, request.getFromNodeId(), request.getToNodeId());
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
