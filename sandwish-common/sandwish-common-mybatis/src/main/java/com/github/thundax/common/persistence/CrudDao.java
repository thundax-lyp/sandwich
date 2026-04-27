package com.github.thundax.common.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * DAO支持类实现
 *
 * @param <T>
 * @author thundax
 */
public interface CrudDao<T> {

    /**
     * 获取单条数据
     *
     * @param entity 对象
     * @return 对象
     */
    T get(T entity);

    /**
     * 获取多条数据
     *
     * @param idList id数组
     * @return 对象列表
     */
    List<T> getMany(@Param("idList") List<String> idList);

    /**
     * 查询数据列表，如果需要分页，请设置分页对象，如：entity.setPage(new Page<T>());
     *
     * @param entity 过滤条件
     * @return 对象列表
     */
    List<T> findList(T entity);

    /**
     * 插入数据
     *
     * @param entity 对象
     * @return 影响数据数
     */
    int insert(T entity);

    /**
     * 更新数据
     *
     * @param entity 对象
     * @return 影响数据数
     */
    int update(T entity);

    /**
     * 更新显示次序
     *
     * @param entity entity
     * @return 影响数据数
     */
    int updatePriority(T entity);

    /**
     * 更新删除标记
     *
     * @param entity entity
     * @return 影响数据数
     */
    int updateDelFlag(T entity);

    /**
     * 删除数据（一般为逻辑删除，更新del_flag字段为1）
     *
     * @param entity 对象，必须设置id
     * @return 影响数据数
     */
    int delete(T entity);
}
