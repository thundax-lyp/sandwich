package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.exception.*;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.SetUtils;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.auth.security.annotation.RequiresPermissions;
import com.github.thundax.modules.sys.assembler.OfficeInterfaceAssembler;
import com.github.thundax.modules.sys.api.OfficeServiceApi;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.request.OfficeIdRequest;
import com.github.thundax.modules.sys.request.OfficeMoveRequest;
import com.github.thundax.modules.sys.request.OfficeQueryRequest;
import com.github.thundax.modules.sys.request.OfficeSaveRequest;
import com.github.thundax.modules.sys.response.OfficeResponse;
import com.github.thundax.modules.sys.service.OfficeService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final OfficeInterfaceAssembler officeInterfaceAssembler;

    @Autowired
    public OfficeApiController(OfficeService officeService,
                               Validator validator,
                               OfficeInterfaceAssembler officeInterfaceAssembler) {
        super(validator);
        this.officeService = officeService;
        this.officeInterfaceAssembler = officeInterfaceAssembler;
    }


    @Override
    @RequiresPermissions("sys:office:view")
    public OfficeResponse get(@RequestBody OfficeIdRequest request) throws ApiException {
        Office bean = officeService.get(request.getId());
        if (bean == null) {
            throw new NullBeanException(Office.BEAN_NAME, request.getId());
        }
        return officeInterfaceAssembler.toResponse(bean);
    }


    @Override
    @RequiresPermissions("sys:office:view")
    public List<OfficeResponse> list(@RequestBody OfficeQueryRequest request) throws ApiException {
        validate(request);

        Office query = new Office();
        Office.Query queryCondition = new Office.Query();

        queryCondition.setParentId(request.getParentId());
        queryCondition.setName(request.getName());
        queryCondition.setRemarks(request.getRemarks());
        query.setQuery(queryCondition);

        return ListUtils.map(officeService.findList(query), officeInterfaceAssembler::toResponse);
    }


    @Override
    @RequiresPermissions("sys:office:edit")
    public OfficeResponse add(@RequestBody OfficeSaveRequest request) throws ApiException {
        validate(request);

        Office entity = officeInterfaceAssembler.toEntity(new Office(), request);
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

        return officeInterfaceAssembler.toResponse(entity);
    }


    @Override
    @RequiresPermissions("sys:office:edit")
    public OfficeResponse update(@RequestBody OfficeSaveRequest request) throws ApiException {
        validate(request);

        Office bean = officeService.get(request.getId());
        if (bean == null) {
            throw new InvalidParameterException("id");
        }

        if (StringUtils.isNotEmpty(request.getParentId())) {
            Office parent = officeService.get(request.getParentId());
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        Office entity = officeInterfaceAssembler.toEntity(bean, request);

        officeService.save(entity);

        return officeInterfaceAssembler.toResponse(entity);
    }


    @Override
    @RequiresPermissions("sys:office:edit")
    public Boolean delete(@RequestBody List<OfficeIdRequest> list) throws ApiException {
        List<Office> beanList = validateList(list, vo -> officeService.get(vo.getId()), null, null);

        officeService.delete(beanList);

        return true;
    }


    @Override
    @RequiresPermissions("sys:office:view")
    public List<OfficeResponse> tree(@RequestBody List<OfficeIdRequest> excludeList) {
        List<Office> beanList = officeService.findList(new Office());

        Set<String> excludeIds = SetUtils.newHashSet(ListUtils.map(excludeList, OfficeIdRequest::getId));
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

        return ListUtils.map(beanList, officeInterfaceAssembler::toTreeResponse);
    }


    @Override
    @RequiresPermissions("sys:office:edit")
    public Boolean move(@RequestBody OfficeMoveRequest request) throws ApiException {
        validate(request);

        Office fromBean = officeService.get(request.getFromNodeId());
        if (fromBean == null) {
            throw new NullBeanException(Office.BEAN_NAME, request.getFromNodeId());
        }

        Office toBean = officeService.get(request.getToNodeId());
        if (toBean == null) {
            throw new NullBeanException(Office.BEAN_NAME, request.getToNodeId());
        }

        if (toBean.equals(fromBean) || officeService.isChildOf(toBean, fromBean)) {
            throw new MoveTreeNodeException(Office.BEAN_NAME, request.getFromNodeId(), request.getToNodeId());
        }

        officeService.moveTreeNode(fromBean, toBean, readMoveTreeNodeType(request));

        return true;
    }

    private TreeService.MoveTreeNodeType readMoveTreeNodeType(OfficeMoveRequest request) {
        switch (request.getType()) {
            case OfficeMoveRequest.TYPE_BEFORE:
                return TreeService.MoveTreeNodeType.BEFORE;
            case OfficeMoveRequest.TYPE_INSIDE:
                return TreeService.MoveTreeNodeType.INSIDE;
            case OfficeMoveRequest.TYPE_INSIDE_LAST:
                return TreeService.MoveTreeNodeType.INSIDE_LAST;
            default:
                return TreeService.MoveTreeNodeType.AFTER;
        }
    }
}
