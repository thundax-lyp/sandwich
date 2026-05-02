package com.github.thundax.modules.sys.service;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.List;

public interface DictService extends CrudService<Dict> {

    List<Dict> list(DictQuery query);

    Page<Dict> page(DictQuery query, Page<Dict> page);

    /**
     * 获取类型列表
     *
     * @return 类型列表
     */
    List<String> listTypes();

    /**
     * 根据类型获取标签
     *
     * @param type 类型
     * @return
     */
    List<String> listLabels(String type);

    /**
     * 获取字典修订号。
     *
     * @return 字典修订号
     */
    String getDictionaryRevision();
}
