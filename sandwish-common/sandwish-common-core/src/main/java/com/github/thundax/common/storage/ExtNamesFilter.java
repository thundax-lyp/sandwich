package com.github.thundax.common.storage;

import com.github.thundax.common.storage.filter.ResourceFilter;
import com.google.common.collect.Lists;
import java.util.List;

/** 资源过滤器：根据resource.extName过滤 */
public class ExtNamesFilter implements ResourceFilter {

    private List<String> extNameList;

    public ExtNamesFilter(String[] extNames) {
        extNameList = Lists.newArrayList();
        if (extNames != null) {
            for (String extName : extNames) {
                extNameList.add(extName.toLowerCase());
            }
        }
    }

    @Override
    public boolean test(Resource resource) {
        if (resource.isFile()) {
            return extNameList.contains(resource.getExtName().toLowerCase());
        }
        return true;
    }
}
