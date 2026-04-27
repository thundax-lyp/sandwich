package com.github.thundax.modules.sys.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.DictDO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 字典 MyBatis Mapper。
 */
@MyBatisDao
public interface DictMapper extends BaseMapper<DictDO> {

    @Update("UPDATE sys_dict "
            + "SET value = #{value}, label = #{label}, type = #{type}, priority = #{priority}, "
            + "remarks = #{remarks}, update_date = #{updateDate}, update_by = #{updateUserId}, "
            + "del_flag = #{delFlag} "
            + "WHERE id = #{id}")
    int update(DictDO dict);

    @Update("UPDATE sys_dict "
            + "SET priority = #{priority}, update_date = #{updateDate}, update_by = #{updateUserId} "
            + "WHERE id = #{id}")
    int updatePriority(DictDO dict);

    @Update("UPDATE sys_dict "
            + "SET del_flag = #{delFlag}, update_date = #{updateDate}, update_by = #{updateUserId} "
            + "WHERE id = #{id}")
    int updateDelFlag(DictDO dict);

    /**
     * 获取类型列表。
     *
     * @return 类型列表
     */
    @Select("SELECT type FROM sys_dict GROUP BY type ORDER BY type")
    List<String> findTypeList();
}
