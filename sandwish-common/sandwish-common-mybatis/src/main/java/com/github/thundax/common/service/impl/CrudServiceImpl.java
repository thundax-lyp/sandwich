package com.github.thundax.common.service.impl;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.service.impl.support.PageHelperResult;
import com.github.thundax.common.utils.StringUtils;
import com.google.common.collect.Maps;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;

/**
 * @param <D>
 * @param <T>
 * @author thundax
 */
@Transactional(readOnly = true)
public abstract class CrudServiceImpl<D extends CrudDao<T>, T extends DataEntity<T>>
        extends BaseServiceImpl {

    public CrudServiceImpl(D dao) {
        this.dao = dao;
    }

    private Class<T> elementType;

    protected D dao;

    @PostConstruct
    @SuppressWarnings("unchecked")
    protected void initService() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            Type[] params = ((ParameterizedType) superClass).getActualTypeArguments();
            if (params[1] instanceof Class) {
                elementType = (Class<T>) params[1];
            }
        }

        initialize();
    }

    public Class<T> getElementType() {
        return elementType;
    }

    public T newEntity(String id) {
        if (elementType != null) {
            try {
                return elementType.getConstructor(String.class).newInstance(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected void initialize() {}

    /** 获取单条数据 */
    public T get(T query) {
        return dao.get(query);
    }

    /** 获取单条数据 */
    public T get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return get(newEntity(id));
    }

    /** 获取单条数据 */
    public List<T> getMany(List<String> ids) {
        return dao.getMany(ids);
    }

    /**
     * 查询列表数据
     *
     * @param query 过滤条件
     */
    public List<T> findList(T query) {
        return dao.findList(query);
    }

    /**
     * 查询单个
     *
     * @param query 过滤条件
     */
    public T findOne(T query) {
        return findOne(() -> this.findList(query));
    }

    public T findOne(ISelect select) {
        try {
            PageHelperResult<T> helperPage =
                    PageHelperResult.startPage(Page.FIRST_PAGE_INDEX, 1, false);

            select.doSelect();

            return helperPage.getFirst();

        } finally {
            PageHelper.clearPage();
        }
    }

    public Page<T> findPage(T query, Page<T> page) {
        return findPage(page, () -> this.findList(query));
    }

    public Page<T> findPage(Page<T> page, ISelect select) {
        try {
            PageHelperResult<T> helperPage =
                    PageHelperResult.startPage(page.getPageNo(), page.getPageSize());

            select.doSelect();

            page.setPageNo(helperPage.getPageNo());
            page.setPageSize(helperPage.getPageSize());
            page.setCount(helperPage.getTotal());
            page.setList(new ArrayList<>(helperPage.getList()));

        } finally {
            PageHelper.clearPage();
        }
        return page;
    }

    public long count(T entity) {
        return PageHelper.count(() -> dao.findList(entity));
    }

    public long count(ISelect select) {
        return PageHelper.count(select);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(T entity) {
        if (entity.getIsNewRecord()) {
            entity.preInsert();
            dao.insert(entity);
        } else {
            entity.preUpdate();
            dao.update(entity);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(T entity) {
        entity.preUpdate();

        int count = dao.updatePriority(entity);
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<T> list) {
        return batchOperate(list, this::updatePriority);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateDelFlag(T entity) {
        entity.preUpdate();

        int count = dao.updateDelFlag(entity);
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateDelFlag(List<T> list) {
        return batchOperate(list, this::updateDelFlag);
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(T entity) {
        int count = dao.delete(entity);
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(List<T> list) {
        return batchOperate(list, this::delete);
    }

    protected int batchOperate(Collection<T> collection, Function<T, Integer> operator) {
        int count = 0;
        if (ListUtils.isNotEmpty(collection)) {
            for (T entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    protected Map<String, String> combineListMap(List<Map<String, String>> mapList) {
        Map<String, String> result = Maps.newHashMap();
        if (mapList != null) {
            for (Map<String, String> map : mapList) {
                result.put(map.get("key"), map.get("value"));
            }
        }
        return result;
    }
}
