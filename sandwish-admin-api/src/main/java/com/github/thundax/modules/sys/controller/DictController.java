package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.request.RequestListHelper;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.DictInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.DictIdRequest;
import com.github.thundax.modules.sys.controller.request.DictPageRequest;
import com.github.thundax.modules.sys.controller.request.DictQueryRequest;
import com.github.thundax.modules.sys.controller.request.DictSaveRequest;
import com.github.thundax.modules.sys.controller.response.DictResponse;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.DictService;
import com.github.thundax.modules.sys.service.query.DictQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "系统/字典")
@SysLogger(module = {"系统", "字典"})
@RequestMapping(value = "/api/sys/dict")
@WrappedApiController
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @ApiOperation(value = "获取对象", notes = "sys:dict:view")
    @HasPermission("sys:dict:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    public DictResponse get(@Valid @RequestBody DictIdRequest request) throws ApiException {
        return DictInterfaceAssembler.toResponse(dictService.getById(EntityIdCodec.toDomain(request.getId())));
    }

    @ApiOperation(value = "获取列表", notes = "sys:dict:view")
    @HasPermission("sys:dict:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public List<DictResponse> list(@Valid @RequestBody DictQueryRequest request) throws ApiException {
        DictQuery query = DictInterfaceAssembler.toQuery(request);
        return dictService.list(query).stream()
                .map(dict -> DictInterfaceAssembler.toResponse(dict))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "获取分页列表", notes = "sys:dict:view")
    @HasPermission("sys:dict:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("分页")
    @RequestMapping(value = "page", method = RequestMethod.POST)
    public PageResponse<DictResponse> page(@Valid @RequestBody DictPageRequest request) throws ApiException {
        DictQuery query = DictInterfaceAssembler.toQuery(request);
        PageDTO<Dict> page = readDictPage(request);
        return PageResponseHelper.fromEntityPage(dictService.page(query, page), DictInterfaceAssembler::toResponse);
    }

    @ApiOperation(value = "添加", notes = "sys:dict:edit")
    @HasPermission("sys:dict:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public DictResponse add(@Valid @RequestBody DictSaveRequest request) throws ApiException {
        Dict dict = DictInterfaceAssembler.toEntity(new Dict(), request);
        dictService.add(dict);
        return DictInterfaceAssembler.toResponse(dict);
    }

    @ApiOperation(value = "更新", notes = "sys:dict:edit")
    @HasPermission("sys:dict:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public DictResponse update(@Valid @RequestBody DictSaveRequest request) throws ApiException {
        Dict dict = dictService.getById(EntityIdCodec.toDomain(request.getId()));
        if (dict == null) {
            throw new ApiException("id not exist");
        }
        Dict entity = DictInterfaceAssembler.toEntity(dict, request);
        dictService.update(entity);
        return DictInterfaceAssembler.toResponse(entity);
    }

    @ApiOperation(value = "删除", notes = "sys:dict:edit")
    @HasPermission("sys:dict:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public Boolean delete(@Valid @RequestBody List<DictIdRequest> list) throws ApiException {
        List<Dict> beanList = new ArrayList<>();
        for (DictIdRequest request : RequestListHelper.present(list)) {
            Dict bean = dictService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException("Dict", EntityIdCodec.toDomain(request.getId()));
            }
            beanList.add(bean);
        }
        if (beanList.isEmpty()) {
            throw new InvalidParameterException("list");
        }
        dictService.batchDeleteById(beanList.stream().map(Dict::getId).collect(Collectors.toList()));
        return true;
    }

    private PageDTO<Dict> readDictPage(DictPageRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();

        if (pageNo == null || pageNo < PageRules.firstPageIndex()) {
            pageNo = PageRules.firstPageIndex();
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = PageRules.defaultPageSize();
        }

        PageDTO<Dict> page = new PageDTO<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }
}
