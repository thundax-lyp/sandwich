package com.github.thundax.modules.sys.service;

import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.List;

public interface DictService extends CrudService<Dict> {

    List<Dict> list(DictQuery query);

    PageDTO<Dict> page(DictQuery query, PageDTO<Dict> page);

    List<String> listTypes();

    List<String> listLabels(String type);

    String getDictionaryRevision();
}
