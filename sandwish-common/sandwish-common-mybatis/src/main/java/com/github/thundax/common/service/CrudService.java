package com.github.thundax.common.service;

import com.github.thundax.common.persistence.Page;

import java.util.List;

/**
 * @param <T> Class
 * @author thundax
 */
public interface CrudService<T> extends BaseService {

    /**
     * 对象类型
     *
     * @return 对象类型
     */
    Class<T> getElementType();

    /**
     * 新对象
     *
     * @param id id
     * @return 对象
     */
    T newEntity(String id);

    /**
     * 获取对象
     *
     * @param entity 对象
     * @return 对象
     */
    T get(T entity);

    /**
     * 获取对象
     *
     * @param id id
     * @return 对象
     */
    T get(String id);

    /**
     * 根据id获取多条数据
     *
     * @param ids id list
     * @return 对象列表
     */
    List<T> getMany(List<String> ids);

    /**
     * 获取列表
     *
     * @param entity 查询条件
     * @return 列表
     */
    List<T> findList(T entity);

    /**
     * 获取对象
     *
     * @param entity 查询条件
     * @return 对象
     */
    T findOne(T entity);

    /**
     * 获取分页数据
     *
     * @param entity 查询条件
     * @param page   分页
     * @return 分页
     */
    Page<T> findPage(T entity, Page<T> page);

    /**
     * 获取 count
     *
     * @param entity 查询条件
     * @return count
     */
    long count(T entity);

    /**
     * 保存，新增或更新
     *
     * @param entity 对象
     */
    void save(T entity);

    /**
     * 删除
     *
     * @param entity 对象
     * @return 影响记录数
     */
    int delete(T entity);

    /**
     * 删除
     *
     * @param list 列表
     * @return 影响记录数
     */
    int delete(List<T> list);

    /**
     * 排序
     *
     * @param entity 对象
     * @return 影响记录数
     */
    int updatePriority(T entity);

    /**
     * 排序
     *
     * @param list 列表
     * @return 影响记录数
     */
    int updatePriority(List<T> list);

    /**
     * 更新删除标记
     *
     * @param entity 对象
     * @return 影响记录数
     */
    int updateDelFlag(T entity);

    /**
     * 更新删除标记
     *
     * @param list 列表
     * @return 影响记录数
     */
    int updateDelFlag(List<T> list);

}
