package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.DictService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** @author wdit */
@Service
@Transactional(readOnly = true)
public class DictServiceImpl extends CrudServiceImpl<DictDao, Dict> implements DictService {

    public DictServiceImpl(DictDao dao) {
        super(dao);
    }

    @Override
    public List<String> findTypeList() {
        return dao.findTypeList();
    }

    public List<String> findLabelList(String type) {
        List<String> result = new ArrayList<String>();
        Dict query = new Dict();
        Dict.Query queryCondition = new Dict.Query();
        queryCondition.setType(type);
        query.setQuery(queryCondition);
        List<Dict> list = dao.findList(query);
        String s = "";
        for (Dict item : list) {
            s = item.getLabel();
            if (StringUtils.isNotEmpty(s)) {
                result.add(s);
            }
        }
        return result;
    }

    @Override
    public List<Dict> findList(Dict dict) {
        return dao.findList(dict);
    }

    @Override
    public String getDictionaryRevision() {
        return dao.getDictionaryRevision();
    }
}
