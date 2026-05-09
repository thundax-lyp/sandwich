package com.github.thundax.modules.sys.persistence.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Dict;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class DictCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "sys.dict.";
    private static final String ID_PREFIX = "id_";
    private static final String KEY_INDEX = "keys";

    @CreateCache(
            name = CACHE_SECTION,
            cacheType = CacheType.REMOTE,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Object> cache;

    @CreateCache(
            name = CACHE_SECTION + "keys.",
            cacheType = CacheType.REMOTE,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<String>> keyIndexCache;

    public Dict getById(Long id) {
        return toDomain((DictCacheDTO) cache.get(objectKey(id)));
    }

    public void putById(Dict dict) {
        if (dict != null && EntityIdCodec.toValue(dict.getId()) != null) {
            String key = objectKey(EntityIdCodec.toValue(dict.getId()));
            cache.put(key, toCacheDTO(dict), OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
            rememberKey(key);
        }
    }

    public void removeById(Long id) {
        String key = objectKey(id);
        cache.remove(key);
        forgetKey(key);
    }

    public void removeAll() {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys != null && !keys.isEmpty()) {
            cache.removeAll(keys);
        }
        keyIndexCache.remove(KEY_INDEX);
    }

    private String objectKey(Long id) {
        return CACHE_SECTION + ID_PREFIX + id;
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

    private static Dict toDomain(DictCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        Dict dict = new Dict();
        dict.setId(EntityIdCodec.toDomain(cacheDTO.id));
        dict.setType(cacheDTO.type);
        dict.setLabel(cacheDTO.label);
        dict.setValue(cacheDTO.value);
        dict.setPriority(cacheDTO.priority == null ? 0 : cacheDTO.priority);
        dict.setRemarks(cacheDTO.remarks);
        dict.setCreateDate(cacheDTO.createDate);
        dict.setCreateUserId(cacheDTO.createUserId);
        dict.setUpdateDate(cacheDTO.updateDate);
        dict.setUpdateUserId(cacheDTO.updateUserId);
        return dict;
    }

    private static DictCacheDTO toCacheDTO(Dict dict) {
        DictCacheDTO cacheDTO = new DictCacheDTO();
        cacheDTO.id = EntityIdCodec.toValue(dict.getId());
        cacheDTO.type = dict.getType();
        cacheDTO.label = dict.getLabel();
        cacheDTO.value = dict.getValue();
        cacheDTO.priority = dict.getPriority();
        cacheDTO.remarks = dict.getRemarks();
        cacheDTO.createDate = dict.getCreateDate();
        cacheDTO.createUserId = dict.getCreateUserId();
        cacheDTO.updateDate = dict.getUpdateDate();
        cacheDTO.updateUserId = dict.getUpdateUserId();
        return cacheDTO;
    }

    private static class DictCacheDTO implements CacheDTO {
        private Long id;
        private String type;
        private String label;
        private String value;
        private Integer priority;
        private String remarks;
        private Date createDate;
        private String createUserId;
        private Date updateDate;
        private String updateUserId;
    }
}
