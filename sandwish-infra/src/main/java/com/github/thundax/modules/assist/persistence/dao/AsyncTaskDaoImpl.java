package com.github.thundax.modules.assist.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.assist.dao.AsyncTaskDao;
import com.github.thundax.modules.assist.entity.AsyncTask;
import com.github.thundax.modules.assist.entity.enums.AsyncTaskStatus;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Repository;

@Repository
public class AsyncTaskDaoImpl implements AsyncTaskDao {

    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "assist.asyncTask.";

    @CreateCache(name = CACHE_SECTION, cacheType = CacheType.REMOTE)
    private Cache<String, AsyncTaskCacheDTO> cache;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    @Override
    public AsyncTask getById(EntityId id) {
        return toDomain(cache.get(cacheKey(String.valueOf(id.value()))));
    }

    @Override
    public EntityId insert(AsyncTask asyncTask) {
        if (asyncTask.getId() == null) {
            asyncTask.setId(idGenerator.nextId());
        }
        Date now = new Date();
        String currentUserId = UserAccessHolder.currentUserId();
        asyncTask.setCreateDate(now);
        asyncTask.setCreateUserId(currentUserId);
        asyncTask.setUpdateDate(now);
        asyncTask.setUpdateUserId(currentUserId);
        put(asyncTask);
        return asyncTask.getId();
    }

    @Override
    public void update(AsyncTask asyncTask) {
        asyncTask.setUpdateDate(new Date());
        asyncTask.setUpdateUserId(UserAccessHolder.currentUserId());
        put(asyncTask);
    }

    @Override
    public void deleteById(EntityId id) {
        cache.remove(cacheKey(EntityIdCodec.toStringValue(id)));
    }

    private String cacheKey(String id) {
        return CACHE_SECTION + id;
    }

    private void put(AsyncTask asyncTask) {
        cache.put(
                cacheKey(EntityIdCodec.toStringValue(asyncTask.getId())),
                toCacheDTO(asyncTask),
                asyncTask.getExpiredSeconds(),
                TimeUnit.SECONDS);
    }

    private static AsyncTask toDomain(AsyncTaskCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        AsyncTask asyncTask = new AsyncTask();
        asyncTask.setId(EntityIdCodec.toDomain(cacheDTO.id));
        asyncTask.setTitle(cacheDTO.title);
        asyncTask.setStatus(cacheDTO.status == null ? null : AsyncTaskStatus.from(cacheDTO.status));
        asyncTask.setMessage(cacheDTO.message);
        asyncTask.setData(cacheDTO.data);
        asyncTask.setPrivate(cacheDTO.isPrivate);
        asyncTask.setExpiredSeconds(cacheDTO.expiredSeconds);
        asyncTask.setPriority(cacheDTO.priority == null ? 0 : cacheDTO.priority);
        asyncTask.setRemarks(cacheDTO.remarks);
        asyncTask.setCreateDate(cacheDTO.createDate);
        asyncTask.setCreateUserId(cacheDTO.createUserId);
        asyncTask.setUpdateDate(cacheDTO.updateDate);
        asyncTask.setUpdateUserId(cacheDTO.updateUserId);
        return asyncTask;
    }

    private static AsyncTaskCacheDTO toCacheDTO(AsyncTask asyncTask) {
        AsyncTaskCacheDTO cacheDTO = new AsyncTaskCacheDTO();
        cacheDTO.id = EntityIdCodec.toValue(asyncTask.getId());
        cacheDTO.title = asyncTask.getTitle();
        cacheDTO.status =
                asyncTask.getStatus() == null ? null : asyncTask.getStatus().value();
        cacheDTO.message = asyncTask.getMessage();
        cacheDTO.data = asyncTask.getData();
        cacheDTO.isPrivate = asyncTask.getPrivate();
        cacheDTO.expiredSeconds = asyncTask.getExpiredSeconds();
        cacheDTO.priority = asyncTask.getPriority();
        cacheDTO.remarks = asyncTask.getRemarks();
        cacheDTO.createDate = asyncTask.getCreateDate();
        cacheDTO.createUserId = asyncTask.getCreateUserId();
        cacheDTO.updateDate = asyncTask.getUpdateDate();
        cacheDTO.updateUserId = asyncTask.getUpdateUserId();
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
        private Date createDate;
        private String createUserId;
        private Date updateDate;
        private String updateUserId;
    }
}
