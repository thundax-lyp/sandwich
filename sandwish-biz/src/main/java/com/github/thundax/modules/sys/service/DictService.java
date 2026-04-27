package com.github.thundax.modules.sys.service;

import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.Dict;

import java.util.List;

/**
 * @author wdit
 */
public interface DictService extends CrudService<Dict> {

    /**
     * 获取类型列表
     *
     * @return 类型列表
     */
    List<String> findTypeList();

    /**
     * 根据类型获取标签
     * @param type 类型
     * @return
     */
    List<String> findLabelList(String type);

    /**
     * 获取字典修订号。
     *
     * @return 字典修订号
     */
    String getDictionaryRevision();

}
