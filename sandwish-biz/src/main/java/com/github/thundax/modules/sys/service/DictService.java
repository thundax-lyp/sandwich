package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.List;

public interface DictService {

    Dict getById(EntityId id);

    List<Dict> list(DictQuery query);

    PageDTO<Dict> page(DictQuery query, PageDTO<Dict> page);

    void add(Dict dict);

    void update(Dict dict);

    int batchDeleteById(List<EntityId> ids);

    List<String> listTypes();

    List<String> listLabels(String type);

    String getDictionaryRevision();
}
