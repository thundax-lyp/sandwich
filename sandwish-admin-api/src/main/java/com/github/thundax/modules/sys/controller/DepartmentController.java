package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.exception.AdminResponseExceptions;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.token.AccessTokenNames;
import com.github.thundax.common.tree.TreeNodeListHelper;
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
import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.service.DepartmentService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "系统/部门")
@SysLogger(module = {"系统", "部门"})
@RequestMapping(value = "/api/sys/department")
@WrappedApiController
public class DepartmentController {

    private static final String DEPARTMENT_NAME = "department";

    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @ApiOperation(value = "获取对象", notes = "sys:department:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:view")
    @SysLogger("读取")
    @PostMapping(value = "get")
    public DepartmentResponse get(@Valid @RequestBody DepartmentIdRequest request) {
        Department bean = departmentService.get(DepartmentIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        return DepartmentInterfaceAssembler.toResponse(bean, departmentService::get);
    }

    @ApiOperation(value = "获取列表", notes = "sys:department:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:view")
    @SysLogger("列表")
    @PostMapping(value = "list")
    public List<DepartmentResponse> list(@Valid @RequestBody DepartmentQueryRequest request) {
        DepartmentQuery query = DepartmentInterfaceAssembler.toQuery(request);

        return departmentService.list(query).stream()
                .map(department -> DepartmentInterfaceAssembler.toResponse(department, departmentService::get))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "添加", notes = "sys:department:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:edit")
    @SysLogger("添加")
    @PostMapping(value = "create")
    public DepartmentResponse add(@Valid @RequestBody DepartmentSaveRequest request) {
        Department entity = DepartmentInterfaceAssembler.toEntity(new Department(), request);
        if (entity.getId() != null) {
            Department bean = departmentService.get(entity.getId());
            if (bean != null) {
                throw AdminResponseExceptions.objectExists();
            }
        }

        if (entity.getParentId() != null) {
            Department parent = departmentService.get(entity.getParentId());
            if (parent == null) {
                throw AdminResponseExceptions.invalidParameter("parentId");
            }
        }

        entity.setId(departmentService.create(DepartmentInterfaceAssembler.toCreateCommand(request)));

        return DepartmentInterfaceAssembler.toResponse(entity, departmentService::get);
    }

    @ApiOperation(value = "更新", notes = "sys:department:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:edit")
    @SysLogger("更新")
    @PostMapping(value = "update")
    public DepartmentResponse update(@Valid @RequestBody DepartmentSaveRequest request) {
        Department bean = departmentService.get(DepartmentIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.invalidParameter("id");
        }

        if (request.getParentId() != null) {
            Department parent = departmentService.get(DepartmentIdCodec.toDomain(request.getParentId()));
            if (parent == null) {
                throw AdminResponseExceptions.invalidParameter("parentId");
            }
        }

        Department entity = DepartmentInterfaceAssembler.toEntity(bean, request);

        departmentService.changeInfo(DepartmentInterfaceAssembler.toChangeInfoCommand(request));

        return DepartmentInterfaceAssembler.toResponse(entity, departmentService::get);
    }

    @ApiOperation(value = "删除", notes = "sys:department:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:edit")
    @SysLogger("删除")
    @PostMapping(value = "delete")
    public Boolean delete(@Valid @RequestBody List<DepartmentIdRequest> list) {
        List<DepartmentId> idList = new ArrayList<>();
        for (DepartmentIdRequest request : RequestListHelper.present(list)) {
            Department bean = departmentService.get(DepartmentIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            idList.add(bean.getId());
        }
        if (idList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        idList.forEach(departmentService::remove);

        return true;
    }

    @ApiOperation(value = "获取列表", notes = "super")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("super")
    @SysLogger("读取")
    @PostMapping(value = "tree")
    public List<DepartmentResponse> tree(@Valid @RequestBody List<DepartmentIdRequest> excludeList) {
        List<Department> beanList = departmentService.list(new DepartmentQuery());

        Set<DepartmentId> excludeIds = new HashSet<>(
                RequestListHelper.map(excludeList, request -> DepartmentIdCodec.toDomain(request.getId())));
        beanList.removeIf(bean -> excludeIds.contains(bean.getId()));

        TreeNodeListHelper.remove(
                beanList,
                new TreeNodeListHelper.TreeNodeSupport<Department, DepartmentId>() {

                    @Override
                    public DepartmentId getId(Department entity) {
                        return entity.getId();
                    }

                    @Override
                    public DepartmentId getParentId(Department entity) {
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
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:department:edit")
    @SysLogger("移动")
    @PostMapping(value = "move")
    public Boolean move(@Valid @RequestBody DepartmentMoveRequest request) {
        Department fromBean = departmentService.get(DepartmentIdCodec.toDomain(request.getFromNodeId()));
        if (fromBean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }

        Department toBean = departmentService.get(DepartmentIdCodec.toDomain(request.getToNodeId()));
        if (toBean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }

        if (toBean.equals(fromBean) || departmentService.existsChildRelation(childRelationQuery(toBean, fromBean))) {
            throw AdminResponseExceptions.moveTreeNode();
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
}
