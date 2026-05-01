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
import com.github.thundax.modules.sys.assembler.OfficeInterfaceAssembler;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.request.OfficeIdRequest;
import com.github.thundax.modules.sys.request.OfficeMoveRequest;
import com.github.thundax.modules.sys.request.OfficeQueryRequest;
import com.github.thundax.modules.sys.request.OfficeSaveRequest;
import com.github.thundax.modules.sys.response.OfficeResponse;
import com.github.thundax.modules.sys.service.OfficeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "02-02.系统-组织机构")
@SysLogger(module = {"系统", "组织机构"})
@RequestMapping(value = "/api/sys/office")
@RestController
public class OfficeApiController extends BaseApiController {

    private final OfficeService officeService;

    @Autowired
    public OfficeApiController(OfficeService officeService) {
        this.officeService = officeService;
    }

    @ApiOperation(value = "获取对象", notes = "sys:office:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:office:view')")
    public OfficeResponse get(@Valid @RequestBody OfficeIdRequest request) throws ApiException {
        Office bean = officeService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Office.BEAN_NAME, request.getId());
        }
        return OfficeInterfaceAssembler.toResponse(bean, officeService::getById);
    }

    @ApiOperation(value = "获取列表", notes = "sys:office:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:office:view')")
    public List<OfficeResponse> list(@Valid @RequestBody OfficeQueryRequest request) throws ApiException {
        Office query = new Office();
        Office.Query queryCondition = new Office.Query();

        queryCondition.setParentId(request.getParentId());
        queryCondition.setName(request.getName());
        queryCondition.setRemarks(request.getRemarks());
        query.setQuery(queryCondition);

        return officeService.list(query).stream()
                .map(office -> OfficeInterfaceAssembler.toResponse(office, officeService::getById))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "添加", notes = "sys:office:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:office:edit')")
    public OfficeResponse add(@Valid @RequestBody OfficeSaveRequest request) throws ApiException {
        Office entity = OfficeInterfaceAssembler.toEntity(new Office(), request);
        if (entity.getId() != null) {
            Office bean = officeService.getById(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(Office.BEAN_NAME, EntityIdCodec.toValue(entity.getId()));
            }
        }

        if (StringUtils.isNotEmpty(entity.getParentId())) {
            Office parent = officeService.getById(EntityIdCodec.toDomain(entity.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        officeService.add(entity);

        return OfficeInterfaceAssembler.toResponse(entity, officeService::getById);
    }

    @ApiOperation(value = "更新", notes = "sys:office:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:office:edit')")
    public OfficeResponse update(@Valid @RequestBody OfficeSaveRequest request) throws ApiException {
        Office bean = officeService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new InvalidParameterException("id");
        }

        if (StringUtils.isNotEmpty(request.getParentId())) {
            Office parent = officeService.getById(EntityIdCodec.toDomain(request.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        Office entity = OfficeInterfaceAssembler.toEntity(bean, request);

        officeService.update(entity);

        return OfficeInterfaceAssembler.toResponse(entity, officeService::getById);
    }

    @ApiOperation(value = "删除", notes = "sys:office:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:office:edit')")
    public Boolean delete(@RequestBody List<OfficeIdRequest> list) throws ApiException {
        List<Office> beanList =
                validateList(list, vo -> officeService.getById(EntityIdCodec.toDomain(vo.getId())), null, null);

        officeService.batchDeleteById(beanList);

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
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:office:view')")
    public List<OfficeResponse> tree(@RequestBody List<OfficeIdRequest> excludeList) {
        List<Office> beanList = officeService.list(new Office());

        Set<String> excludeIds = excludeList == null
                ? new HashSet<>()
                : new HashSet<>(
                        excludeList.stream().map(request -> request.getId()).collect(Collectors.toList()));
        beanList.removeIf(bean -> excludeIds.contains(EntityIdCodec.toValue(bean.getId())));

        removeTreeNode(
                beanList,
                new RemoveTreeNodeSupport<Office>() {

                    @Override
                    public String getId(Office entity) {
                        return EntityIdCodec.toValue(entity.getId());
                    }

                    @Override
                    public String getParentId(Office entity) {
                        return entity.getParentId();
                    }

                    @Override
                    public boolean isRoot(Office entity) {
                        return StringUtils.isBlank(entity.getParentId());
                    }
                },
                excludeIds);

        return beanList.stream()
                .map(office -> OfficeInterfaceAssembler.toTreeResponse(office))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "移动", notes = "sys:office:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("移动")
    @RequestMapping(value = "move", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:office:edit')")
    public Boolean move(@Valid @RequestBody OfficeMoveRequest request) throws ApiException {
        Office fromBean = officeService.getById(EntityIdCodec.toDomain(request.getFromNodeId()));
        if (fromBean == null) {
            throw new NullBeanException(Office.BEAN_NAME, request.getFromNodeId());
        }

        Office toBean = officeService.getById(EntityIdCodec.toDomain(request.getToNodeId()));
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
