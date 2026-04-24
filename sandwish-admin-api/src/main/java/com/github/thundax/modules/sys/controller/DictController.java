package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.sys.api.DictServiceApi;
import com.github.thundax.modules.sys.api.query.DictQueryParam;
import com.github.thundax.modules.sys.api.vo.DictVo;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.DictService;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;
import java.util.List;

@RestController
public class DictController extends BaseApiController implements DictServiceApi {

    private final DictService dictService;

    public DictController(Validator validator, DictService dictService) {
        super(validator);
        this.dictService = dictService;
    }

    @Override
    public DictVo get(@RequestBody DictVo dict) throws ApiException {
        return entityToVo(dictService.get(new Dict(dict.getId())));
    }

    @Override
    public List<DictVo> list(@RequestBody DictQueryParam queryParam) throws ApiException {
        Dict query = new Dict();
        if(StringUtils.isNotEmpty(queryParam.getLabel())){
            query.setQueryProp(Dict.Query.PROP_LABEL,queryParam.getLabel());
        }
        if(StringUtils.isNotEmpty(queryParam.getType())){
            query.setQueryProp(Dict.Query.PROP_TYPE,queryParam.getType());
        }
        if(StringUtils.isNotEmpty(queryParam.getRemarks())){
            query.setQueryProp(Dict.Query.PROP_REMARKS,queryParam.getRemarks());
        }
        return  ListUtils.map(dictService.findList(query), this::entityToVo);
    }

    @Override
    public PageVo<DictVo> page(@RequestBody DictQueryParam queryParam) throws ApiException {
        Dict query = new Dict();
        if(StringUtils.isNotEmpty(queryParam.getLabel())){
            query.setQueryProp(Dict.Query.PROP_LABEL,queryParam.getLabel());
        }
        if(StringUtils.isNotEmpty(queryParam.getType())){
            query.setQueryProp(Dict.Query.PROP_TYPE,queryParam.getType());
        }
        if(StringUtils.isNotEmpty(queryParam.getRemarks())){
            query.setQueryProp(Dict.Query.PROP_REMARKS,queryParam.getRemarks());
        }
        Page<Dict> page = readPage(queryParam);
        return  entityPageToVo(dictService.findPage(query,page), this::entityToVo);
    }

    @Override
    public DictVo add(@RequestBody DictVo vo) throws ApiException {
        Dict dict = VoToEntity(vo, new Dict());
        dictService.save(dict);
        return entityToVo(dict);
    }

    @Override
    public DictVo update(@RequestBody DictVo vo) throws ApiException {
        Dict dict = dictService.get(new Dict(vo.getId()));
        if (dict == null) {
            throw new ApiException("id not exist");
        }
        Dict entity = VoToEntity(vo, dict);
        dictService.save(entity);
        return entityToVo(entity);
    }

    @Override
    public Boolean delete(@RequestBody List<DictVo> list) throws ApiException {
        List<Dict> beanList = validateList(list,
                vo -> dictService.get(vo.getId()),
                null, null);
        dictService.delete(beanList);
        return true;
    }


    private DictVo entityToVo(Dict entity){
        if (entity == null) {
            return new DictVo();
        }
        DictVo vo = new DictVo();
        baseEntityToVo(vo,entity);
        vo.setLabel(entity.getLabel());
        vo.setType(entity.getType());
        vo.setValue(entity.getValue());
        vo.setRemarks(entity.getRemarks());
        vo.setPriority(entity.getPriority());
        return vo;
    }

    @NonNull
    private Dict VoToEntity(@NonNull DictVo vo, @NonNull Dict entity){
        baseVoToEntity(entity,vo);
        entity.setLabel(vo.getLabel());
        entity.setType(vo.getType());
        entity.setValue(vo.getValue());
        entity.setRemarks(vo.getRemarks());
        entity.setPriority(vo.getPriority());
        return entity;
    }
}
