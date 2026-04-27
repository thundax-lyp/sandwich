package com.github.thundax.modules.sys.utils;

import com.google.common.collect.Lists;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.MapUtils;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author wdit
 */
@Service
@Lazy(false)
public class DictServiceHolder {

    private static DictService service;

    private static final Map<String, List<Dict>> TYPE_DICT_MAP = MapUtils.newConcurrentMap();
    private static String lastCacheVersion = IdGen.uuid();

    private static final PooledThreadLocal<Map<String, Dict>> ID_OBJECT_HOLDER = new PooledThreadLocal<>();
    private static final PooledThreadLocal<String> CACHE_VERSION_HOLDER = new PooledThreadLocal<>();

    @Autowired
    public DictServiceHolder(DictService targetService) {
        service = targetService;
    }

    public static DictService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(DictService.class);
        }
        return service;
    }

    public static Dict get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return ID_OBJECT_HOLDER
                .computeIfAbsent(MapUtils::newHashMap)
                .computeIfAbsent(id, (key) -> getService().get(id));
    }

    public static synchronized List<Dict> getDictList(String type) {
        String currentCacheVersion = CACHE_VERSION_HOLDER.computeIfAbsent(() -> getService().getDictionaryRevision());

        if (!StringUtils.equals(currentCacheVersion, lastCacheVersion)) {
            TYPE_DICT_MAP.clear();
            for (Dict dict : getService().findList(new Dict())) {
                TYPE_DICT_MAP.computeIfAbsent(dict.getType(), key -> ListUtils.newArrayList()).add(dict);
            }
            lastCacheVersion = currentCacheVersion;
        }

        List<Dict> list = TYPE_DICT_MAP.get(type);
        if (list == null) {
            return Lists.newArrayList();
        }
        return list;
    }

    public static String getDictLabel(String value, String type, String defaultValue) {
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(value)) {
            for (Dict dict : getDictList(type)) {
                if (type.equals(dict.getType()) && value.equals(dict.getValue())) {
                    return dict.getLabel();
                }
            }
        }
        return defaultValue;
    }

}
