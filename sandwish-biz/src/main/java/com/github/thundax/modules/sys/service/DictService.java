package com.github.thundax.modules.sys.service;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.List;

public interface DictService extends CrudService<Dict> {

    List<Dict> list(DictQuery query);

    Page<Dict> page(DictQuery query, Page<Dict> page);

    List<String> listTypes();

    List<String> listLabels(String type);

    String getDictionaryRevision();
}
