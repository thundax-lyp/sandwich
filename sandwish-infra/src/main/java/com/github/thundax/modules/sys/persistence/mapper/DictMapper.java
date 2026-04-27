package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.DictDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典 MyBatis Mapper。
 */
@MyBatisDao
public interface DictMapper {

    DictDO get(DictDO dict);

    List<DictDO> getMany(@Param("idList") List<String> idList);

    List<DictDO> findList(DictDO dict);

    int insert(DictDO dict);

    int update(DictDO dict);

    int updatePriority(DictDO dict);

    int updateStatus(DictDO dict);

    int updateDelFlag(DictDO dict);

    int delete(DictDO dict);

    /**
     * 获取类型列表。
     *
     * @return 类型列表
     */
    List<String> findTypeList();
}
