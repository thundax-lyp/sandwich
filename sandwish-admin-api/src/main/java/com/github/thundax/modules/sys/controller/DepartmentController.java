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
import com.github.thundax.modules.sys.assembler.DepartmentInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.DepartmentIdRequest;
import com.github.thundax.modules.sys.controller.request.DepartmentMoveRequest;
import com.github.thundax.modules.sys.controller.request.DepartmentQueryRequest;
import com.github.thundax.modules.sys.controller.request.DepartmentSaveRequest;
import com.github.thundax.modules.sys.controller.response.DepartmentResponse;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "系统/部门")
@SysLogger(module = {"系统", "部门"})
@RequestMapping(value = "/api/sys/department")
@WrappedApiController
public class DepartmentController {

    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @ApiOperation(value = "获取对象", notes = "sys:department:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:view")
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    public DepartmentResponse get(@Valid @RequestBody DepartmentIdRequest request) throws ApiException {
        Department bean = departmentService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Department.BEAN_NAME, request.getId());
        }
        return DepartmentInterfaceAssembler.toResponse(bean, departmentService::getById);
    }

    @ApiOperation(value = "获取列表", notes = "sys:department:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:view")
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public List<DepartmentResponse> list(@Valid @RequestBody DepartmentQueryRequest request) throws ApiException {
        DepartmentQuery query = DepartmentInterfaceAssembler.toQuery(request);

        return departmentService.list(query).stream()
                .map(department -> DepartmentInterfaceAssembler.toResponse(department, departmentService::getById))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "添加", notes = "sys:department:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:edit")
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public DepartmentResponse add(@Valid @RequestBody DepartmentSaveRequest request) throws ApiException {
        Department entity = DepartmentInterfaceAssembler.toEntity(new Department(), request);
        if (entity.getId() != null) {
            Department bean = departmentService.getById(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(Department.BEAN_NAME, EntityIdCodec.toValue(entity.getId()));
            }
        }

        if (StringUtils.isNotEmpty(entity.getParentId())) {
            Department parent = departmentService.getById(EntityIdCodec.toDomain(entity.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        departmentService.add(entity);

        return DepartmentInterfaceAssembler.toResponse(entity, departmentService::getById);
    }

    @ApiOperation(value = "更新", notes = "sys:department:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:edit")
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public DepartmentResponse update(@Valid @RequestBody DepartmentSaveRequest request) throws ApiException {
        Department bean = departmentService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new InvalidParameterException("id");
        }

        if (StringUtils.isNotEmpty(request.getParentId())) {
            Department parent = departmentService.getById(EntityIdCodec.toDomain(request.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        Department entity = DepartmentInterfaceAssembler.toEntity(bean, request);

        departmentService.update(entity);

        return DepartmentInterfaceAssembler.toResponse(entity, departmentService::getById);
    }

    @ApiOperation(value = "删除", notes = "sys:department:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:edit")
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public Boolean delete(@Valid @RequestBody List<DepartmentIdRequest> list) throws ApiException {
        List<Department> beanList = new ArrayList<>();
        for (DepartmentIdRequest request : RequestListHelper.present(list)) {
            Department bean = departmentService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Department.BEAN_NAME, request.getId());
            }
            beanList.add(bean);
        }
        if (beanList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        departmentService.batchDeleteById(
                beanList.stream().map(Department::getId).collect(Collectors.toList()));

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
    @HasPermission("super")
    @SysLogger("读取")
    @RequestMapping(value = "tree", method = RequestMethod.POST)
    public List<DepartmentResponse> tree(@Valid @RequestBody List<DepartmentIdRequest> excludeList) {
        List<Department> beanList = departmentService.listAll();

        Set<String> excludeIds = new HashSet<>(RequestListHelper.map(excludeList, DepartmentIdRequest::getId));
        beanList.removeIf(bean -> excludeIds.contains(EntityIdCodec.toValue(bean.getId())));

        TreeNodeListHelper.remove(
                beanList,
                new TreeNodeListHelper.TreeNodeSupport<Department>() {

                    @Override
                    public String getId(Department entity) {
                        return EntityIdCodec.toValue(entity.getId());
                    }

                    @Override
                    public String getParentId(Department entity) {
                        return entity.getParentId();
                    }

                    @Override
                    public boolean isRoot(Department entity) {
                        return StringUtils.isBlank(entity.getParentId());
                    }
                },
                excludeIds);

        return beanList.stream()
                .map(department -> DepartmentInterfaceAssembler.toTreeResponse(department))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "移动", notes = "sys:department:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:edit")
    @SysLogger("移动")
    @RequestMapping(value = "move", method = RequestMethod.POST)
    public Boolean move(@Valid @RequestBody DepartmentMoveRequest request) throws ApiException {
        Department fromBean = departmentService.getById(EntityIdCodec.toDomain(request.getFromNodeId()));
        if (fromBean == null) {
            throw new NullBeanException(Department.BEAN_NAME, request.getFromNodeId());
        }

        Department toBean = departmentService.getById(EntityIdCodec.toDomain(request.getToNodeId()));
        if (toBean == null) {
            throw new NullBeanException(Department.BEAN_NAME, request.getToNodeId());
        }

        if (toBean.equals(fromBean) || departmentService.isChildOf(toBean, fromBean)) {
            throw new MoveTreeNodeException(Department.BEAN_NAME, request.getFromNodeId(), request.getToNodeId());
        }

        departmentService.moveTreeNode(fromBean, toBean, readMoveTreeNodeType(request));

        return true;
    }

    private TreeNodeMoveType readMoveTreeNodeType(DepartmentMoveRequest request) {
        switch (request.getType()) {
            case DepartmentMoveRequest.TYPE_BEFORE:
                return TreeNodeMoveType.BEFORE;
            case DepartmentMoveRequest.TYPE_INSIDE:
                return TreeNodeMoveType.INSIDE;
            case DepartmentMoveRequest.TYPE_INSIDE_LAST:
                return TreeNodeMoveType.INSIDE_LAST;
            default:
                return TreeNodeMoveType.AFTER;
        }
    }
}
