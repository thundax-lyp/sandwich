package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.Dict;
import java.util.List;

public interface DictDao {

    Dict getById(EntityId id);

    List<Dict> batchGetByIds(List<String> idList);

    List<Dict> list(String type, String label, String remarks);

    Page<Dict> page(String type, String label, String remarks, int pageNo, int pageSize);

    String insert(Dict dict);

    int update(Dict dict);

    int updatePriority(Dict dict);

    int deleteById(EntityId id);

    /**
     * 获取类型列表
     *
     * @return 类型列表
     */
    List<String> listTypes();

    /**
     * 获取字典修订号。
     *
     * @return 字典修订号
     */
    String getDictionaryRevision();
}
