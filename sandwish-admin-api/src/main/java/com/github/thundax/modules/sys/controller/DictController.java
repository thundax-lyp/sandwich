package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.web.ApiRequestListHelper;
import com.github.thundax.common.web.PageVoHelper;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.DictInterfaceAssembler;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.request.DictIdRequest;
import com.github.thundax.modules.sys.request.DictPageRequest;
import com.github.thundax.modules.sys.request.DictQueryRequest;
import com.github.thundax.modules.sys.request.DictSaveRequest;
import com.github.thundax.modules.sys.response.DictResponse;
import com.github.thundax.modules.sys.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "02-05.系统-字典")
@SysLogger(module = {"系统", "字典"})
@RequestMapping(value = "/api/sys/dict")
@RestController
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @ApiOperation(value = "获取对象", notes = "sys:dict:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    public DictResponse get(@RequestBody DictIdRequest request) throws ApiException {
        return DictInterfaceAssembler.toResponse(dictService.getById(EntityIdCodec.toDomain(request.getId())));
    }

    @ApiOperation(value = "获取列表", notes = "sys:dict:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public List<DictResponse> list(@RequestBody DictQueryRequest request) throws ApiException {
        Dict query = readQuery(request.getLabel(), request.getType(), request.getRemarks());
        return dictService.list(query).stream()
                .map(dict -> DictInterfaceAssembler.toResponse(dict))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "获取分页列表", notes = "sys:dict:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("分页")
    @RequestMapping(value = "page", method = RequestMethod.POST)
    public PageVo<DictResponse> page(@RequestBody DictPageRequest request) throws ApiException {
        Dict query = readQuery(request.getLabel(), request.getType(), request.getRemarks());
        Page<Dict> page = readDictPage(request);
        return PageVoHelper.fromEntityPage(dictService.page(query, page), DictInterfaceAssembler::toResponse);
    }

    @ApiOperation(value = "添加", notes = "sys:dict:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public DictResponse add(@RequestBody DictSaveRequest request) throws ApiException {
        Dict dict = DictInterfaceAssembler.toEntity(new Dict(), request);
        dictService.add(dict);
        return DictInterfaceAssembler.toResponse(dict);
    }

    @ApiOperation(value = "更新", notes = "sys:dict:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public DictResponse update(@RequestBody DictSaveRequest request) throws ApiException {
        Dict dict = dictService.getById(EntityIdCodec.toDomain(request.getId()));
        if (dict == null) {
            throw new ApiException("id not exist");
        }
        Dict entity = DictInterfaceAssembler.toEntity(dict, request);
        dictService.update(entity);
        return DictInterfaceAssembler.toResponse(entity);
    }

    @ApiOperation(value = "删除", notes = "sys:dict:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public Boolean delete(@RequestBody List<DictIdRequest> list) throws ApiException {
        List<Dict> beanList = ApiRequestListHelper.mapNotEmpty(list, request -> {
            Dict bean = dictService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException("Dict", request.getId());
            }
            return bean;
        });
        dictService.batchDeleteById(beanList);
        return true;
    }

    private Dict readQuery(String label, String type, String remarks) {
        Dict query = new Dict();
        Dict.Query queryCondition = new Dict.Query();
        if (StringUtils.isNotEmpty(label)) {
            queryCondition.setLabel(label);
        }
        if (StringUtils.isNotEmpty(type)) {
            queryCondition.setType(type);
        }
        if (StringUtils.isNotEmpty(remarks)) {
            queryCondition.setRemarks(remarks);
        }
        query.setQuery(queryCondition);
        return query;
    }

    private Page<Dict> readDictPage(DictPageRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();

        if (pageNo == null || pageNo < Page.FIRST_PAGE_INDEX) {
            pageNo = Page.FIRST_PAGE_INDEX;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = Page.DEFAULT_PAGE_SIZE;
        }

        Page<Dict> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }
}
