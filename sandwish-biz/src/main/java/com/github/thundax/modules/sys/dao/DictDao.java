package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.sys.entity.Dict;
import java.util.List;

public interface DictDao {

    Dict get(String id);

    List<Dict> getMany(List<String> idList);

    List<Dict> findList(String type, String label, String remarks);

    Page<Dict> findPage(String type, String label, String remarks, int pageNo, int pageSize);

    String insert(Dict dict);

    int update(Dict dict);

    int updatePriority(Dict dict);

    int updateDelFlag(Dict dict);

    int delete(String id);

    /**
     * 获取类型列表
     *
     * @return 类型列表
     */
    List<String> findTypeList();

    /**
     * 获取字典修订号。
     *
     * @return 字典修订号
     */
    String getDictionaryRevision();
}
