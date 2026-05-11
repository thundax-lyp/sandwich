package com.github.thundax.modules.sys.service;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.entity.valueobject.DictId;
import com.github.thundax.modules.sys.service.command.ChangeDictInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateDictCommand;
import com.github.thundax.modules.sys.service.command.DeleteDictCommand;
import com.github.thundax.modules.sys.service.command.DictSortCommand;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.List;

public interface DictService {

    Dict get(DictId id);

    List<Dict> list(DictQuery query);

    PageResult<Dict> page(DictQuery query, PageQuery page);

    DictId create(CreateDictCommand command);

    void sort(DictSortCommand command);

    void changeInfo(ChangeDictInfoCommand command);

    void remove(DeleteDictCommand command);

    List<String> listTypes(DictQuery query);

    List<String> listLabels(DictQuery query);
}
