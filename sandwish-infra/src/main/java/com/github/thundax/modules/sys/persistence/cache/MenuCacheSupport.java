package com.github.thundax.modules.sys.persistence.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.common.Constants;
import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.modules.sys.codec.AccessRankCodec;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.MenuIdCodec;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class MenuCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final String CACHE_SECTION = Constants.CACHE_PREFIX + "sys.menu.";
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

    public Menu getById(Long id) {
        return toDomain((MenuCacheDTO) cache.get(String.valueOf(id)));
    }

    public void putById(Menu menu) {
        if (menu != null && MenuIdCodec.toValue(menu.getId()) != null) {
            String key = String.valueOf(MenuIdCodec.toValue(menu.getId()));
            cache.put(key, toCacheDTO(menu), OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
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

    private static Menu toDomain(MenuCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return null;
        }
        Menu menu = new Menu();
        menu.setId(MenuIdCodec.toDomain(cacheDTO.id));
        menu.setParentId(MenuIdCodec.toDomain(cacheDTO.parentId));
        menu.setName(cacheDTO.name);
        menu.setPerms(cacheDTO.perms);
        menu.setRank(AccessRankCodec.toDomain(cacheDTO.rank));
        menu.setVisibility(cacheDTO.visibility == null ? null : MenuVisibility.from(cacheDTO.visibility));
        menu.setDisplayParams(cacheDTO.displayParams);
        menu.setUrl(cacheDTO.url);
        menu.setTarget(cacheDTO.target);
        menu.setPriority(cacheDTO.priority == null ? 0 : cacheDTO.priority);
        menu.setRemarks(cacheDTO.remarks);
        return menu;
    }

    private static MenuCacheDTO toCacheDTO(Menu menu) {
        MenuCacheDTO cacheDTO = new MenuCacheDTO();
        cacheDTO.id = MenuIdCodec.toValue(menu.getId());
        cacheDTO.parentId = MenuIdCodec.toValue(menu.getParentId());
        cacheDTO.name = menu.getName();
        cacheDTO.perms = menu.getPerms();
        cacheDTO.rank = AccessRankCodec.toValue(menu.getRank());
        cacheDTO.visibility =
                menu.getVisibility() == null ? null : menu.getVisibility().value();
        cacheDTO.displayParams = menu.getDisplayParams();
        cacheDTO.url = menu.getUrl();
        cacheDTO.target = menu.getTarget();
        cacheDTO.priority = menu.getPriority();
        cacheDTO.remarks = menu.getRemarks();
        return cacheDTO;
    }

    private static class MenuCacheDTO implements CacheDTO {
        private Long id;
        private Long parentId;
        private String name;
        private String perms;
        private Integer rank;
        private String visibility;
        private String displayParams;
        private String url;
        private String target;
        private Integer priority;
        private String remarks;
    }
}
