package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.exception.*;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.SetUtils;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.vo.query.MoveTreeNodeQueryParam;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.auth.security.annotation.RequiresPermissions;
import com.github.thundax.modules.sys.api.MenuServiceApi;
import com.github.thundax.modules.sys.api.query.MenuQueryParam;
import com.github.thundax.modules.sys.api.vo.MenuVo;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;
import java.util.List;
import java.util.Set;

/**
 * @author thundax
 */
@RestController
public class MenuApiController extends BaseApiController implements MenuServiceApi {

    private final MenuService menuService;

    @Autowired
    public MenuApiController(MenuService menuService, Validator validator) {
        super(validator);
        this.menuService = menuService;
    }


    @Override
    @RequiresPermissions("super")
    public MenuVo get(@RequestBody MenuVo vo) throws ApiException {
        Menu bean = menuService.get(vo.getId());
        if (bean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, vo.getId());
        }
        return entityToVo(bean);
    }


    @Override
    @RequiresPermissions("super")
    public List<MenuVo> list(@RequestBody MenuQueryParam queryParam) throws ApiException {
        validate(queryParam);

        Menu query = new Menu();

        query.setQueryProp(Menu.Query.PROP_PARENT_ID, queryParam.getParentId());
        if (queryParam.getDisplay() != null) {
            query.setQueryProp(Menu.Query.PROP_DISPLAY_FLAG, queryParam.getDisplay() ? Global.SHOW : Global.HIDE);
        }

        return ListUtils.map(menuService.findList(query), this::entityToVo);
    }


    @Override
    @RequiresPermissions("super")
    public MenuVo add(@RequestBody MenuVo vo) throws ApiException {
        validate(vo);

        Menu entity = voToEntity(new Menu(), vo);
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

        return entityToVo(entity);
    }


    @Override
    @RequiresPermissions("super")
    public MenuVo update(@RequestBody MenuVo vo) throws ApiException {
        validate(vo);

        Menu bean = menuService.get(vo.getId());
        if (bean == null) {
            throw new InvalidParameterException("id");
        }

        if (StringUtils.isNotEmpty(vo.getParentId())) {
            Menu parent = menuService.get(vo.getParentId());
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        Menu entity = voToEntity(bean, vo);

        menuService.save(entity);

        return entityToVo(entity);
    }


    @Override
    @RequiresPermissions("super")
    public Boolean updateDisplayFlag(@RequestBody List<MenuVo> list) throws ApiException {
        List<Menu> beanList = validateList(list,
                vo -> menuService.get(vo.getId()),
                null,
                (bean, vo) -> bean.setDisplayFlag(Boolean.TRUE.equals(vo.getDisplay()) ? Global.SHOW : Global.HIDE));

        menuService.updateDisplayFlag(beanList);

        return true;
    }


    @Override
    @RequiresPermissions("super")
    public Boolean delete(@RequestBody List<MenuVo> list) throws ApiException {
        List<Menu> beanList = validateList(list, vo -> menuService.get(vo.getId()), null, null);

        menuService.delete(beanList);

        return true;
    }

    @Override
    @RequiresPermissions("super")
    public List<MenuVo> tree(@RequestBody List<MenuVo> excludeList) {
        List<Menu> beanList = menuService.findList(new Menu());

        Set<String> excludeIds = SetUtils.newHashSet(ListUtils.map(excludeList, MenuVo::getId));
        beanList.removeIf(bean -> excludeIds.contains(bean.getId()));

        removeTreeNode(beanList, new RemoveTreeNodeSupport<Menu>() {

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

        }, excludeIds);

        return ListUtils.map(beanList, entity -> {
            MenuVo vo = new MenuVo(entity.getId());
            vo.setParentId(entity.getParentId());
            vo.setName(entity.getName());
            return vo;
        });
    }


    @Override
    @RequiresPermissions("super")
    public Boolean move(@RequestBody MoveTreeNodeQueryParam queryParam) throws ApiException {
        validate(queryParam);

        Menu fromBean = menuService.get(queryParam.getFromNodeId());
        if (fromBean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, queryParam.getFromNodeId());
        }

        Menu toBean = menuService.get(queryParam.getToNodeId());
        if (toBean == null) {
            throw new NullBeanException(Menu.BEAN_NAME, queryParam.getToNodeId());
        }

        if (toBean.equals(fromBean) || toBean.isChildOf(fromBean, null)) {
            throw new MoveTreeNodeException(Menu.BEAN_NAME, queryParam.getFromNodeId(), queryParam.getToNodeId());
        }

        menuService.moveTreeNode(fromBean, toBean, readMoveTreeNodeType(queryParam));

        return true;
    }


    @NonNull
    private MenuVo entityToVo(Menu entity) {
        if (entity == null) {
            return new MenuVo();
        }

        MenuVo vo = baseEntityToVo(new MenuVo(), entity);

        if (StringUtils.isNotEmpty(entity.getParentId())) {
            vo.setParentId(entity.getParentId());
        }
        vo.setName(entity.getName());
        vo.setPerms(entity.getPerms());
        vo.setRanks(entity.getRanks());

        vo.setDisplay(entity.isDisplay());
        vo.setDisplayParams(entity.getDisplayParams());
        vo.setUrl(entity.getUrl());

        return vo;
    }

    @NonNull
    private Menu voToEntity(@NonNull Menu entity, @NonNull MenuVo vo) {
        baseVoToEntity(entity, vo);

        if (StringUtils.isNotEmpty(vo.getParentId())) {
            entity.setParentId(vo.getParentId());
        }
        entity.setName(vo.getName());
        entity.setPerms(vo.getPerms());
        entity.setRanks(vo.getRanks());

        entity.setDisplayFlag(Boolean.TRUE.equals(vo.getDisplay()) ? Global.SHOW : Global.HIDE);
        entity.setDisplayParams(vo.getDisplayParams());
        entity.setUrl(vo.getUrl());

        return entity;
    }

}
