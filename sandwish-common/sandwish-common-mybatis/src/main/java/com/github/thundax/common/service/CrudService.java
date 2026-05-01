package com.github.thundax.common.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.persistence.Page;
import java.util.List;

/**
 * 通用 CRUD 服务接口。
 *
 * @param <T> 实体类型
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
    T getById(T entity);

    /**
     * 获取对象
     *
     * @param id id
     * @return 对象
     */
    T getById(EntityId id);

    /**
     * 根据id获取多条数据
     *
     * @param ids id list
     * @return 对象列表
     */
    List<T> batchGetByIds(List<String> ids);

    /**
     * 获取列表
     *
     * @param entity 查询条件
     * @return 列表
     */
    List<T> list(T entity);

    /**
     * 获取对象
     *
     * @param entity 查询条件
     * @return 对象
     */
    T getOne(T entity);

    /**
     * 获取分页数据
     *
     * @param entity 查询条件
     * @param page 分页
     * @return 分页
     */
    Page<T> page(T entity, Page<T> page);

    /**
     * 获取 count
     *
     * @param entity 查询条件
     * @return count
     */
    long count(T entity);

    /**
     * 新增
     *
     * @param entity 对象
     */
    void add(T entity);

    /**
     * 更新
     *
     * @param entity 对象
     */
    void update(T entity);

    /**
     * 删除
     *
     * @param entity 对象
     * @return 影响记录数
     */
    int deleteById(T entity);

    /**
     * 删除
     *
     * @param list 列表
     * @return 影响记录数
     */
    int batchDeleteById(List<T> list);

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
}
