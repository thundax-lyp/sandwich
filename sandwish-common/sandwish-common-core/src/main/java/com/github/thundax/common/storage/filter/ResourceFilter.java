package com.github.thundax.common.storage.filter;

import com.github.thundax.common.storage.Resource;

public interface ResourceFilter {

    /** 是否允许 */
    boolean test(Resource resource);
}
