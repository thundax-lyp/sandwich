package com.github.thundax.modules.sys.persistence.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class DepartmentCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "SYS_DEPARTMENT_";
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

    public Department getById(Long id) {
        return toDomain((DepartmentCacheDTO) cache.get(String.valueOf(id)));
    }

    public void putById(Department department) {
        if (department != null && DepartmentIdCodec.toValue(department.getId()) != null) {
            String key = String.valueOf(DepartmentIdCodec.toValue(department.getId()));
            cache.put(key, toCacheDTO(department), OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
            rememberKey(key);
        }
    }

    public void removeById(Long id) {
        String key = String.valueOf(id);
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

    private static Department toDomain(DepartmentCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        Department department = new Department();
        department.setId(DepartmentIdCodec.toDomain(cacheDTO.id));
        department.setParentId(DepartmentIdCodec.toDomain(cacheDTO.parentId));
        department.setName(cacheDTO.name);
        department.setShortName(cacheDTO.shortName);
        department.setPriority(cacheDTO.priority == null ? 0 : cacheDTO.priority);
        department.setRemarks(cacheDTO.remarks);
        return department;
    }

    private static DepartmentCacheDTO toCacheDTO(Department department) {
        DepartmentCacheDTO cacheDTO = new DepartmentCacheDTO();
        cacheDTO.id = DepartmentIdCodec.toValue(department.getId());
        cacheDTO.parentId = DepartmentIdCodec.toValue(department.getParentId());
        cacheDTO.name = department.getName();
        cacheDTO.shortName = department.getShortName();
        cacheDTO.priority = department.getPriority();
        cacheDTO.remarks = department.getRemarks();
        return cacheDTO;
    }

    private static class DepartmentCacheDTO implements CacheDTO {
        private Long id;
        private Long parentId;
        private String name;
        private String shortName;
        private Integer priority;
        private String remarks;
    }
}
