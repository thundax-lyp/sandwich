package com.github.thundax.common.storage.filter;

import com.github.thundax.common.storage.Resource;
import com.github.thundax.common.utils.StringUtils;

/** 资源过滤器：根据resource.name前缀过滤 */
public class NamePrefixFilter implements ResourceFilter {

    private String prefix;

    public NamePrefixFilter(String prefix) {
        this.prefix = prefix.toLowerCase();
    }

    @Override
    public boolean test(Resource resource) {
        return StringUtils.startsWithIgnoreCase(resource.getName().toLowerCase(), prefix);
    }
}
