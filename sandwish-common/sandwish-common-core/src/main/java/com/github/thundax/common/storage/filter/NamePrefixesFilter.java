package com.github.thundax.common.storage.filter;

import com.github.thundax.common.storage.Resource;
import com.github.thundax.common.utils.StringUtils;

/**
 * 资源过滤器：根据resource.name前缀过滤
 *
 */
public class NamePrefixesFilter implements ResourceFilter {

    private String[] prefixes;

    public NamePrefixesFilter(String[] prefixes) {
        this.prefixes = prefixes;
        for (int idx = 0; idx < this.prefixes.length; idx++) {
            prefixes[idx] = prefixes[idx].toLowerCase();
        }
    }

    @Override
    public boolean test(Resource resource) {
        return StringUtils.startsWithAny(resource.getName().toLowerCase(), prefixes);
    }
}
