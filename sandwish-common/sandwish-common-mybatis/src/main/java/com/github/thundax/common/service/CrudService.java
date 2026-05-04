package com.github.thundax.common.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import java.util.List;

/**
 * 通用 CRUD 服务接口。
 *
 * @param <T> 实体类型
 */
public interface CrudService<T> {

    Class<T> getElementType();

    T newEntity(String id);

    T getById(T entity);

    T getById(EntityId id);

    List<T> batchGetByIds(List<EntityId> ids);

    List<T> list(T entity);

    T getOne(T entity);

    PageDTO<T> page(T entity, PageDTO<T> page);

    long count(T entity);

    void add(T entity);

    void update(T entity);

    int deleteById(EntityId id);

    int batchDeleteById(List<EntityId> ids);

    int updatePriority(T entity);

    int updatePriority(List<T> list);
}
