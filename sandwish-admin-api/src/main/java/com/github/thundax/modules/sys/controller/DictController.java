package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.persistence.Page;
import org.apache.commons.lang3.StringUtils;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.sys.api.DictServiceApi;
import com.github.thundax.modules.sys.assembler.DictInterfaceAssembler;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.request.DictIdRequest;
import com.github.thundax.modules.sys.request.DictPageRequest;
import com.github.thundax.modules.sys.request.DictQueryRequest;
import com.github.thundax.modules.sys.request.DictSaveRequest;
import com.github.thundax.modules.sys.response.DictResponse;
import com.github.thundax.modules.sys.service.DictService;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Validator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DictController extends BaseApiController implements DictServiceApi {

    private final DictService dictService;
    private final DictInterfaceAssembler dictInterfaceAssembler;

    public DictController(
            Validator validator,
            DictService dictService,
            DictInterfaceAssembler dictInterfaceAssembler) {
        super(validator);
        this.dictService = dictService;
        this.dictInterfaceAssembler = dictInterfaceAssembler;
    }

    @Override
    public DictResponse get(@RequestBody DictIdRequest request) throws ApiException {
        return dictInterfaceAssembler.toResponse(dictService.get(new Dict(request.getId())));
    }

    @Override
    public List<DictResponse> list(@RequestBody DictQueryRequest request) throws ApiException {
        Dict query = readQuery(request.getLabel(), request.getType(), request.getRemarks());
        return dictService.findList(query).stream()
                .map(dict -> dictInterfaceAssembler.toResponse(dict))
                .collect(Collectors.toList());
    }

    @Override
    public PageVo<DictResponse> page(@RequestBody DictPageRequest request) throws ApiException {
        Dict query = readQuery(request.getLabel(), request.getType(), request.getRemarks());
        Page<Dict> page = readDictPage(request);
        return entityPageToVo(
                dictService.findPage(query, page), dictInterfaceAssembler::toResponse);
    }

    @Override
    public DictResponse add(@RequestBody DictSaveRequest request) throws ApiException {
        Dict dict = dictInterfaceAssembler.toEntity(new Dict(), request);
        dictService.save(dict);
        return dictInterfaceAssembler.toResponse(dict);
    }

    @Override
    public DictResponse update(@RequestBody DictSaveRequest request) throws ApiException {
        Dict dict = dictService.get(new Dict(request.getId()));
        if (dict == null) {
            throw new ApiException("id not exist");
        }
        Dict entity = dictInterfaceAssembler.toEntity(dict, request);
        dictService.save(entity);
        return dictInterfaceAssembler.toResponse(entity);
    }

    @Override
    public Boolean delete(@RequestBody List<DictIdRequest> list) throws ApiException {
        List<Dict> beanList = validateList(list, vo -> dictService.get(vo.getId()), null, null);
        dictService.delete(beanList);
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
