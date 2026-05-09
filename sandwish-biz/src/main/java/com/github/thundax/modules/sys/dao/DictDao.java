package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.Dict;
import java.util.List;

public interface DictDao {

    Dict getById(EntityId id);

    List<Dict> listByIds(List<Long> idList);

    List<Dict> list(String type, String label, String remarks);

    Page<Dict> page(String type, String label, String remarks, int pageNo, int pageSize);

    EntityId insert(Dict dict);

    int update(Dict dict);

    int updatePriority(Dict dict);

    int deleteById(EntityId id);

    List<String> listTypes();
}
