package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.exception.*;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.SetUtils;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.vo.query.MoveTreeNodeQueryParam;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.auth.security.annotation.RequiresPermissions;
import com.github.thundax.modules.sys.api.OfficeServiceApi;
import com.github.thundax.modules.sys.api.query.OfficeQueryParam;
import com.github.thundax.modules.sys.api.vo.OfficeVo;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.service.OfficeService;
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
public class OfficeApiController extends BaseApiController implements OfficeServiceApi {

    private final OfficeService officeService;

    @Autowired
    public OfficeApiController(OfficeService officeService, Validator validator) {
        super(validator);
        this.officeService = officeService;
    }


    @Override
    @RequiresPermissions("sys:office:view")
    public OfficeVo get(@RequestBody OfficeVo vo) throws ApiException {
        Office bean = officeService.get(vo.getId());
        if (bean == null) {
            throw new NullBeanException(Office.BEAN_NAME, vo.getId());
        }
        return entityToVo(bean);
    }


    @Override
    @RequiresPermissions("sys:office:view")
    public List<OfficeVo> list(@RequestBody OfficeQueryParam queryParam) throws ApiException {
        validate(queryParam);

        Office query = new Office();

        query.setQueryProp(Office.Query.PROP_PARENT_ID, queryParam.getParentId());
        query.setQueryProp(Office.Query.PROP_NAME, queryParam.getName());
        query.setQueryProp(Office.Query.PROP_REMARKS, queryParam.getRemarks());

        return ListUtils.map(officeService.findList(query), this::entityToVo);
    }


    @Override
    @RequiresPermissions("sys:office:edit")
    public OfficeVo add(@RequestBody OfficeVo vo) throws ApiException {
        validate(vo);

        Office entity = voToEntity(new Office(), vo);
        if (StringUtils.isNotEmpty(entity.getId())) {
            Office bean = officeService.get(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(Office.BEAN_NAME, entity.getId());
            }
            entity.setIsNewRecord(true);
        }

        if (StringUtils.isNotEmpty(entity.getParentId())) {
            Office parent = officeService.get(entity.getParentId());
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        officeService.save(entity);

        return entityToVo(entity);
    }


    @Override
    @RequiresPermissions("sys:office:edit")
    public OfficeVo update(@RequestBody OfficeVo vo) throws ApiException {
        validate(vo);

        Office bean = officeService.get(vo.getId());
        if (bean == null) {
            throw new InvalidParameterException("id");
        }

        if (StringUtils.isNotEmpty(vo.getParentId())) {
            Office parent = officeService.get(vo.getParentId());
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        Office entity = voToEntity(bean, vo);

        officeService.save(entity);

        return entityToVo(entity);
    }


    @Override
    @RequiresPermissions("sys:office:edit")
    public Boolean delete(@RequestBody List<OfficeVo> list) throws ApiException {
        List<Office> beanList = validateList(list, vo -> officeService.get(vo.getId()), null, null);

        officeService.delete(beanList);

        return true;
    }


    @Override
    @RequiresPermissions("sys:office:view")
    public List<OfficeVo> tree(@RequestBody List<OfficeVo> excludeList) {
        List<Office> beanList = officeService.findList(new Office());

        Set<String> excludeIds = SetUtils.newHashSet(ListUtils.map(excludeList, OfficeVo::getId));
        beanList.removeIf(bean -> excludeIds.contains(bean.getId()));

        removeTreeNode(beanList, new RemoveTreeNodeSupport<Office>() {

            @Override
            public String getId(Office entity) {
                return entity.getId();
            }

            @Override
            public String getParentId(Office entity) {
                return entity.getParentId();
            }

            @Override
            public boolean isRoot(Office entity) {
                return StringUtils.isBlank(entity.getParentId());
            }

        }, excludeIds);

        return ListUtils.map(beanList, entity -> {
            OfficeVo vo = new OfficeVo(entity.getId());
            if (StringUtils.isNotBlank(entity.getParentId())) {
                vo.setParentId(entity.getParentId());
            }
            vo.setName(entity.getName());
            vo.setShortName(entity.getShortName());
            return vo;
        });
    }


    @Override
    @RequiresPermissions("sys:office:edit")
    public Boolean move(@RequestBody MoveTreeNodeQueryParam queryParam) throws ApiException {
        validate(queryParam);

        Office fromBean = officeService.get(queryParam.getFromNodeId());
        if (fromBean == null) {
            throw new NullBeanException(Office.BEAN_NAME, queryParam.getFromNodeId());
        }

        Office toBean = officeService.get(queryParam.getToNodeId());
        if (toBean == null) {
            throw new NullBeanException(Office.BEAN_NAME, queryParam.getToNodeId());
        }

        if (toBean.equals(fromBean) || toBean.isChildOf(fromBean, null)) {
            throw new MoveTreeNodeException(Office.BEAN_NAME, queryParam.getFromNodeId(), queryParam.getToNodeId());
        }

        officeService.moveTreeNode(fromBean, toBean, readMoveTreeNodeType(queryParam));

        return true;
    }

    @NonNull
    private OfficeVo entityToVo(Office entity) {
        if (entity == null) {
            return new OfficeVo();
        }

        OfficeVo vo = baseEntityToVo(new OfficeVo(), entity);

        if (StringUtils.isNotEmpty(entity.getParentId())) {
            vo.setParentId(entity.getParentId());
        }
        vo.setName(entity.getName());
        vo.setShortName(entity.getShortName());
        vo.setNamePath(entity.getNamePath());

        return vo;
    }

    @NonNull
    private Office voToEntity(@NonNull Office entity, @NonNull OfficeVo vo) {
        baseVoToEntity(entity, vo);

        if (StringUtils.isNotEmpty(vo.getParentId())) {
            entity.setParentId(vo.getParentId());
        }
        entity.setName(vo.getName());
        entity.setShortName(vo.getShortName());

        return entity;
    }

}
