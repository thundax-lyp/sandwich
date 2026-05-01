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
import javax.validation.Validator;
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
    public OfficeApiController(OfficeService officeService, Validator validator) {
        super(validator);
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
    public OfficeResponse get(@RequestBody OfficeIdRequest request) throws ApiException {
        Office bean = officeService.get(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Office.BEAN_NAME, request.getId());
        }
        return OfficeInterfaceAssembler.toResponse(bean, officeService::get);
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
    public List<OfficeResponse> list(@RequestBody OfficeQueryRequest request) throws ApiException {
        validate(request);

        Office query = new Office();
        Office.Query queryCondition = new Office.Query();

        queryCondition.setParentId(request.getParentId());
        queryCondition.setName(request.getName());
        queryCondition.setRemarks(request.getRemarks());
        query.setQuery(queryCondition);

        return officeService.findList(query).stream()
                .map(office -> OfficeInterfaceAssembler.toResponse(office, officeService::get))
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
    public OfficeResponse add(@RequestBody OfficeSaveRequest request) throws ApiException {
        validate(request);

        Office entity = OfficeInterfaceAssembler.toEntity(new Office(), request);
        if (entity.getId() != null) {
            Office bean = officeService.get(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(Office.BEAN_NAME, EntityIdCodec.toValue(entity.getId()));
            }
        }

        if (StringUtils.isNotEmpty(entity.getParentId())) {
            Office parent = officeService.get(EntityIdCodec.toDomain(entity.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        officeService.add(entity);

        return OfficeInterfaceAssembler.toResponse(entity, officeService::get);
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
    public OfficeResponse update(@RequestBody OfficeSaveRequest request) throws ApiException {
        validate(request);

        Office bean = officeService.get(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new InvalidParameterException("id");
        }

        if (StringUtils.isNotEmpty(request.getParentId())) {
            Office parent = officeService.get(EntityIdCodec.toDomain(request.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        Office entity = OfficeInterfaceAssembler.toEntity(bean, request);

        officeService.update(entity);

        return OfficeInterfaceAssembler.toResponse(entity, officeService::get);
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
                validateList(list, vo -> officeService.get(EntityIdCodec.toDomain(vo.getId())), null, null);

        officeService.delete(beanList);

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
        List<Office> beanList = officeService.findList(new Office());

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
    public Boolean move(@RequestBody OfficeMoveRequest request) throws ApiException {
        validate(request);

        Office fromBean = officeService.get(EntityIdCodec.toDomain(request.getFromNodeId()));
        if (fromBean == null) {
            throw new NullBeanException(Office.BEAN_NAME, request.getFromNodeId());
        }

        Office toBean = officeService.get(EntityIdCodec.toDomain(request.getToNodeId()));
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
