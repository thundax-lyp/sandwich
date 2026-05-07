package com.github.thundax.modules.sys.persistence.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.modules.sys.entity.Department;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DepartmentCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final int VERSION_EXPIRE_SECONDS = OBJECT_EXPIRE_SECONDS + 5;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "SYS_DEPARTMENT_";
    private static final String ID_PREFIX = "id_";
    private static final String VERSION_KEY = "version";
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
            expire = VERSION_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<String>> keyIndexCache;

    public Department getById(Long id) {
        return toDomain((DepartmentCacheDTO) cache.get(objectKey(id)));
    }

    public void putById(Department department) {
        if (department != null && EntityIdCodec.toValue(department.getId()) != null) {
            String key = objectKey(EntityIdCodec.toValue(department.getId()));
            cache.put(key, toCacheDTO(department), OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
            rememberKey(key);
        }
    }

    public void removeById(Long id) {
        String key = objectKey(id);
        cache.remove(key);
        forgetKey(key);
        touchVersion();
    }

    public void removeAll() {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys != null && !keys.isEmpty()) {
            cache.removeAll(keys);
        }
        keyIndexCache.remove(KEY_INDEX);
        touchVersion();
    }

    public String currentVersion() {
        String version = (String) cache.get(versionKey());
        if (StringUtils.isBlank(version)) {
            version = UuidHelper.compact();
            cache.put(versionKey(), version);
        }
        return version;
    }

    public void touchVersion() {
        cache.put(versionKey(), UuidHelper.compact(), VERSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    private String objectKey(Long id) {
        return CACHE_SECTION + ID_PREFIX + id;
    }

    private String versionKey() {
        return CACHE_SECTION + VERSION_KEY;
    }

    private void rememberKey(String key) {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys == null) {
            keys = new HashSet<>();
        }
        if (keys.add(key)) {
            keyIndexCache.put(KEY_INDEX, keys, VERSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void forgetKey(String key) {
        Set<String> keys = keyIndexCache.get(KEY_INDEX);
        if (keys == null) {
            return;
        }
        if (keys.remove(key)) {
            keyIndexCache.put(KEY_INDEX, keys, VERSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }

    private static Department toDomain(DepartmentCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        Department department = new Department();
        department.setId(EntityIdCodec.toDomain(cacheDTO.id));
        department.setParentId(EntityIdCodec.toDomain(cacheDTO.parentId));
        department.setName(cacheDTO.name);
        department.setShortName(cacheDTO.shortName);
        department.setPriority(cacheDTO.priority == null ? 0 : cacheDTO.priority);
        department.setRemarks(cacheDTO.remarks);
        department.setCreateDate(cacheDTO.createDate);
        department.setCreateUserId(cacheDTO.createUserId);
        department.setUpdateDate(cacheDTO.updateDate);
        department.setUpdateUserId(cacheDTO.updateUserId);
        return department;
    }

    private static DepartmentCacheDTO toCacheDTO(Department department) {
        DepartmentCacheDTO cacheDTO = new DepartmentCacheDTO();
        cacheDTO.id = EntityIdCodec.toValue(department.getId());
        cacheDTO.parentId = EntityIdCodec.toValue(department.getParentId());
        cacheDTO.name = department.getName();
        cacheDTO.shortName = department.getShortName();
        cacheDTO.priority = department.getPriority();
        cacheDTO.remarks = department.getRemarks();
        cacheDTO.createDate = department.getCreateDate();
        cacheDTO.createUserId = department.getCreateUserId();
        cacheDTO.updateDate = department.getUpdateDate();
        cacheDTO.updateUserId = department.getUpdateUserId();
        return cacheDTO;
    }

    private static class DepartmentCacheDTO implements CacheDTO {
        private Long id;
        private Long parentId;
        private String name;
        private String shortName;
        private Integer priority;
        private String remarks;
        private Date createDate;
        private String createUserId;
        private Date updateDate;
        private String updateUserId;
    }
}
