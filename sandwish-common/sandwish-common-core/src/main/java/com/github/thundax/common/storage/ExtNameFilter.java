package com.github.thundax.common.storage;

import com.github.thundax.common.storage.filter.ResourceFilter;

/**
 * 资源过滤器：根据resource.extName过滤
 *
 */
public class ExtNameFilter implements ResourceFilter {

    private String extName;

    public ExtNameFilter(String extName) {
        this.extName = extName.toLowerCase();
    }

    @Override
    public boolean test(Resource resource) {
        if (resource.isFile()) {
            return resource.getExtName().toLowerCase().equals(extName);
        }
        return true;
    }

}
