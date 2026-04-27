package com.github.thundax.common.service.impl;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @param <D>
 * @param <T>
 * @author thundax
 */
@Transactional(readOnly = true)
public abstract class CrudServiceImpl<D extends CrudDao<T>, T extends DataEntity<T>> extends BaseServiceImpl {

    protected static final int MAX_BATCH_COUNT = 500;
    private static final int DEFAULT_CACHE_EXPIRE_SECONDS = 3600;

    private static final String CACHE_VERSION = "version";

    public CrudServiceImpl(D dao, RedisClient redisClient) {
        this.dao = dao;
        this.redisClient = redisClient;
    }

    private Class<T> elementType;

    protected D dao;

    protected RedisClient redisClient;

    protected String cacheSection;
    protected String cacheKeyPrefix;
    protected int cacheExpireSeconds;

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

        if (isRedisCacheEnabled()) {
            cacheSection = this.getCacheSection();
            cacheKeyPrefix = this.getCacheKeyPrefix();
            cacheExpireSeconds = this.getCacheExpireSeconds();
            redisClient.delete(this.cacheSection);
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

    protected void initialize() {

    }

    // 是否开启redis缓存

    /**
     * 是否开启redis缓存
     *
     * @return true:开启；false:不开启
     */
    protected boolean isRedisCacheEnabled() {
        return false;
    }

    /**
     * 设置Cache域名
     *
     * @return cache域名，一般设置为Constance.CACHE_PREFIX + sectionName
     */
    protected String getCacheSection() {
        return getClass().getName() + "_";
    }

    protected String getCacheKey(T entity) {
        return String.valueOf(entity.getId());
    }

    /**
     * 获取对象的Cache key
     */
    private String getCacheKeyPrefix() {
        return "id_";
    }

    /**
     * 获取对象的Cache的expire seconds
     */
    protected int getCacheExpireSeconds() {
        return DEFAULT_CACHE_EXPIRE_SECONDS;
    }

    private T getRedisCache(T entity) {
        return redisClient.get(cacheSection + cacheKeyPrefix + getCacheKey(entity), elementType);
    }

    protected void putRedisCache(T entity) {
        redisClient.set(cacheSection + cacheKeyPrefix + getCacheKey(entity), entity, cacheExpireSeconds);
    }

    protected void removeRedisCache(T entity) {
        redisClient.delete(cacheSection + cacheKeyPrefix + getCacheKey(entity));
        updateCacheVersion();
    }

    public void removeAllCache() {
        if (isRedisCacheEnabled()) {
            redisClient.deleteByPattern(cacheSection + "*");
            updateCacheVersion();
        }
    }

    private void updateCacheVersion() {
        redisClient.set(cacheSection + CACHE_VERSION, IdGen.uuid(), cacheExpireSeconds + 5);
    }

    public String getCacheVersion() {
        String cacheVersion = redisClient.get(cacheSection + CACHE_VERSION);
        if (StringUtils.isBlank(cacheVersion)) {
            cacheVersion = IdGen.uuid();
            redisClient.set(getCacheSection() + CACHE_VERSION, cacheVersion);
        }
        return cacheVersion;
    }

    protected void putCache(T entity) {
        if (isRedisCacheEnabled()) {
            putRedisCache(entity);
        }
    }

    protected void removeCache(T entity) {
        if (isRedisCacheEnabled()) {
            removeRedisCache(entity);
        }
    }

    /**
     * 预加载数据
     */
    public void preload(Collection<String> ids) {
        if (!isRedisCacheEnabled()) {
            logger.warn("called [preload], but redis cache is disabled.");
            return;
        }

        List<String> uncachedIdList = Lists.newArrayList();

        ids.forEach(id -> {
            T entity = getRedisCache(newEntity(id));
            if (entity == null) {
                uncachedIdList.add(id);
            }
        });

        if (ListUtils.isNotEmpty(uncachedIdList)) {
            int pageSize = MAX_BATCH_COUNT;
            int totalPage = (uncachedIdList.size() + pageSize - 1) / pageSize;
            for (int pageNo = 0; pageNo < totalPage; pageNo++) {
                List<String> pageIdList = ListUtils.subList(uncachedIdList, pageSize * pageNo, pageSize);
                dao.getMany(pageIdList).forEach(this::putCache);
            }
        }
    }

    /**
     * 获取单条数据
     */
    public T get(T query) {
        T entity;

        if (isRedisCacheEnabled()) {
            entity = getRedisCache(query);
            if (entity != null) {
                return entity;
            }
        }

        entity = dao.get(query);
        if (entity != null) {
            if (isRedisCacheEnabled()) {
                putRedisCache(entity);
            }
        }

        return entity;
    }


    /**
     * 获取单条数据
     */
    public T get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return get(newEntity(id));
    }


    /**
     * 获取单条数据
     */
    public List<T> getMany(List<String> ids) {
        List<T> entities = ListUtils.newArrayList();

        List<String> uncachedIdList = Lists.newArrayList();
        for (String id : ids) {
            T entity = null;
            T query = newEntity(id);

            if (isRedisCacheEnabled()) {
                entity = getRedisCache(query);
                if (entity != null) {
                    entities.add(entity);
                }
            }

            if (entity == null) {
                uncachedIdList.add(id);
            }
        }

        if (ListUtils.isNotEmpty(uncachedIdList)) {
            dao.getMany(uncachedIdList).forEach(entity -> {
                if (isRedisCacheEnabled()) {
                    putRedisCache(entity);
                }
                entities.add(entity);
            });
        }

        return entities;
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
            com.github.pagehelper.Page<T> helperPage = PageHelper.startPage(Page.FIRST_PAGE_INDEX, 1, false);

            select.doSelect();

            return helperPage.size() > 0 ? helperPage.get(0) : null;

        } finally {
            PageHelper.clearPage();
        }
    }

    public Page<T> findPage(T query, Page<T> page) {
        return findPage(page, () -> this.findList(query));
    }

    public Page<T> findPage(Page<T> page, ISelect select) {
        try {
            com.github.pagehelper.Page<T> helperPage = PageHelper.startPage(page.getPageNo(), page.getPageSize());

            select.doSelect();

            page.setPageNo(helperPage.getPageNum());
            page.setPageSize(helperPage.getPageSize());
            page.setCount(helperPage.getTotal());
            page.setList(new ArrayList<>(helperPage));

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

        removeCache(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(T entity) {
        entity.preUpdate();

        int count = dao.updatePriority(entity);

        removeCache(entity);

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

        removeCache(entity);

        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateDelFlag(List<T> list) {
        return batchOperate(list, this::updateDelFlag);
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(T entity) {
        int count = dao.delete(entity);

        removeCache(entity);

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
