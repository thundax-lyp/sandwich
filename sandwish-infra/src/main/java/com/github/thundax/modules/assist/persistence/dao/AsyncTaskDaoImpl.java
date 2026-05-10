package com.github.thundax.modules.assist.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.enums.AsyncTaskStatus;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskId;
import com.github.thundax.modules.assist.entity.valueobject.AsyncTaskIdCodec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Repository;

@Repository
public class AsyncTaskDaoImpl implements AsyncTaskDao {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "assist.asyncTask.";
    private static final String KEY_INDEX = "keys";

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, AsyncTaskCacheDTO> cache;

    @CreateCache(
            name = CACHE_SECTION + "keys.",
            cacheType = CacheType.REMOTE,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<String>> keyIndexCache;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    @Override
    public AsyncTask getById(AsyncTaskId id) {
        if (id == null) {
            return null;
        }
        return toDomain(cache.get(cacheKey(String.valueOf(id.value()))));
    }

    @Override
    public List<AsyncTask> list(SortDirection sortDirection) {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<AsyncTask> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            AsyncTask task = toDomain(cache.get(cacheKey(key)));
            if (task == null) {
                forgetKey(key);
                continue;
            }
            result.add(task);
        }

        Comparator<AsyncTask> comparator = Comparator
                .comparingInt(AsyncTask::getPriority)
                .thenComparing(task -> task.getId() == null ? Long.MIN_VALUE : task.getId().value());
        if (SortDirection.DESC == sortDirection) {
            comparator = comparator.reversed();
        }
        result.sort(comparator);
        return result;
    }

    @Override
    public int maxPriority() {
        List<AsyncTask> tasks = list(SortDirection.ASC);
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        int max = 0;
        for (AsyncTask task : tasks) {
            max = Math.max(max, task.getPriority());
        }
        return max;
    }

    @Override
    public AsyncTaskId insert(AsyncTask asyncTask) {
        if (asyncTask == null) {
            return null;
        }
        if (asyncTask.getId() == null) {
            asyncTask.setId(AsyncTaskIdCodec.toDomain(idGenerator.nextId().value()));
        }
        put(asyncTask);
        return asyncTask.getId();
    }

    @Override
    public void update(AsyncTask asyncTask) {
        put(asyncTask);
    }

    @Override
    public int updatePriority(AsyncTaskId id, int priority) {
        AsyncTask asyncTask = getById(id);
        if (asyncTask == null) {
            return 0;
        }
        asyncTask.setPriority(priority);
        put(asyncTask);
        return 1;
    }

    @Override
    public void deleteById(AsyncTaskId id) {
        if (id == null) {
            return;
        }
        String key = AsyncTaskIdCodec.toStringValue(id);
        cache.remove(cacheKey(key));
        forgetKey(key);
    }

    private String cacheKey(String id) {
        return CACHE_SECTION + id;
    }

    private void put(AsyncTask asyncTask) {
        if (asyncTask == null || asyncTask.getId() == null) {
            return;
        }
        String key = AsyncTaskIdCodec.toStringValue(asyncTask.getId());
        cache.put(cacheKey(key), toCacheDTO(asyncTask), asyncTask.getExpiredSeconds(), TimeUnit.SECONDS);
        rememberKey(key);
    }

    private void rememberKey(String key) {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys == null) {
            keys = new HashSet<>();
        }
        if (keys.add(key)) {
            keyIndexCache.put(KEY_INDEX, keys, OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void forgetKey(String key) {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys == null) {
            return;
        }
        if (keys.remove(key)) {
            keyIndexCache.put(KEY_INDEX, keys, OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }

    private static AsyncTask toDomain(AsyncTaskCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        AsyncTask asyncTask = new AsyncTask();
        asyncTask.setId(AsyncTaskIdCodec.toDomain(cacheDTO.id));
        asyncTask.setTitle(cacheDTO.title);
        asyncTask.setStatus(cacheDTO.status == null ? null : AsyncTaskStatus.from(cacheDTO.status));
        asyncTask.setMessage(cacheDTO.message);
        asyncTask.setData(cacheDTO.data);
        asyncTask.setPrivate(cacheDTO.isPrivate);
        asyncTask.setExpiredSeconds(cacheDTO.expiredSeconds);
        asyncTask.setPriority(cacheDTO.priority == null ? 0 : cacheDTO.priority);
        asyncTask.setRemarks(cacheDTO.remarks);
        return asyncTask;
    }

    private static AsyncTaskCacheDTO toCacheDTO(AsyncTask asyncTask) {
        AsyncTaskCacheDTO cacheDTO = new AsyncTaskCacheDTO();
        cacheDTO.id = AsyncTaskIdCodec.toValue(asyncTask.getId());
        cacheDTO.title = asyncTask.getTitle();
        cacheDTO.status = asyncTask.getStatus() == null ? null : asyncTask.getStatus().value();
        cacheDTO.message = asyncTask.getMessage();
        cacheDTO.data = asyncTask.getData();
        cacheDTO.isPrivate = asyncTask.getPrivate();
        cacheDTO.expiredSeconds = asyncTask.getExpiredSeconds();
        cacheDTO.priority = asyncTask.getPriority();
        cacheDTO.remarks = asyncTask.getRemarks();
        return cacheDTO;
    }

    private static class AsyncTaskCacheDTO implements CacheDTO {
        private Long id;
        private String title;
        private String status;
        private String message;
        private String data;
        private Boolean isPrivate;
        private Integer expiredSeconds;
        private Integer priority;
        private String remarks;
    }
}
