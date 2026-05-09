package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.collection.TreeNodeListHelper;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.MoveTreeNodeException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.id.EntityId;
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
import com.github.thundax.modules.sys.service.command.DeleteDepartmentCommand;
import com.github.thundax.modules.sys.service.command.MoveDepartmentCommand;
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
        Department bean = departmentService.get(departmentQuery(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Department.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
        }
        return DepartmentInterfaceAssembler.toResponse(bean, this::getDepartment);
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
                .map(department -> DepartmentInterfaceAssembler.toResponse(department, this::getDepartment))
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
            Department bean = departmentService.get(departmentQuery(entity.getId()));
            if (bean != null) {
                throw new InsertBeanExistException(Department.BEAN_NAME, entity.getId());
            }
        }

        if (entity.getParentId() != null) {
            Department parent = departmentService.get(departmentQuery(entity.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        entity.setId(departmentService.create(DepartmentInterfaceAssembler.toCreateCommand(request)));

        return DepartmentInterfaceAssembler.toResponse(entity, this::getDepartment);
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
        Department bean = departmentService.get(departmentQuery(request.getId()));
        if (bean == null) {
            throw new InvalidParameterException("id");
        }

        if (request.getParentId() != null) {
            Department parent = departmentService.get(departmentQuery(request.getParentId()));
            if (parent == null) {
                throw new InvalidParameterException("parentId");
            }
        }

        Department entity = DepartmentInterfaceAssembler.toEntity(bean, request);

        departmentService.changeInfo(DepartmentInterfaceAssembler.toChangeInfoCommand(request));

        return DepartmentInterfaceAssembler.toResponse(entity, this::getDepartment);
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
        List<DeleteDepartmentCommand> commandList = new ArrayList<>();
        for (DepartmentIdRequest request : RequestListHelper.present(list)) {
            Department bean = departmentService.get(departmentQuery(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Department.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
            }
            commandList.add(new DeleteDepartmentCommand(bean.getId()));
        }
        if (commandList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        commandList.forEach(departmentService::remove);

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
        List<Department> beanList = departmentService.list(new DepartmentQuery());

        Set<EntityId> excludeIds =
                new HashSet<>(RequestListHelper.map(excludeList, request -> EntityIdCodec.toDomain(request.getId())));
        beanList.removeIf(bean -> excludeIds.contains(bean.getId()));

        TreeNodeListHelper.remove(
                beanList,
                new TreeNodeListHelper.TreeNodeSupport<Department, EntityId>() {

                    @Override
                    public EntityId getId(Department entity) {
                        return entity.getId();
                    }

                    @Override
                    public EntityId getParentId(Department entity) {
                        return entity.getParentId();
                    }

                    @Override
                    public boolean isRoot(Department entity) {
                        return entity.getParentId() == null;
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
        Department fromBean = departmentService.get(departmentQuery(request.getFromNodeId()));
        if (fromBean == null) {
            throw new NullBeanException(Department.BEAN_NAME, EntityIdCodec.toDomain(request.getFromNodeId()));
        }

        Department toBean = departmentService.get(departmentQuery(request.getToNodeId()));
        if (toBean == null) {
            throw new NullBeanException(Department.BEAN_NAME, EntityIdCodec.toDomain(request.getToNodeId()));
        }

        if (toBean.equals(fromBean) || departmentService.existsChildRelation(childRelationQuery(toBean, fromBean))) {
            throw new MoveTreeNodeException(
                    Department.BEAN_NAME,
                    EntityIdCodec.toDomain(request.getFromNodeId()),
                    EntityIdCodec.toDomain(request.getToNodeId()));
        }

        departmentService.move(
                new MoveDepartmentCommand(fromBean.getId(), toBean.getId(), readMoveTreeNodeType(request)));

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

    private DepartmentQuery childRelationQuery(Department child, Department ancestor) {
        DepartmentQuery query = new DepartmentQuery();
        query.setChildId(child.getId());
        query.setAncestorId(ancestor.getId());
        return query;
    }

    private Department getDepartment(EntityId departmentId) {
        return departmentService.get(departmentQuery(departmentId));
    }

    private DepartmentQuery departmentQuery(EntityId departmentId) {
        DepartmentQuery query = new DepartmentQuery();
        query.setId(departmentId);
        return query;
    }

    private DepartmentQuery departmentQuery(Long departmentId) {
        return departmentQuery(EntityIdCodec.toDomain(departmentId));
    }
}
