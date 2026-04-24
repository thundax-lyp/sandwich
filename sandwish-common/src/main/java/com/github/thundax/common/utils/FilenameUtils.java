package com.github.thundax.common.utils;

import com.github.thundax.common.collect.ListUtils;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * @author thundax
 */
public class FilenameUtils extends org.apache.commons.io.FilenameUtils {

    public static final char SEPARATOR_CHAR = '/';
    public static final String SEPARATOR = "/";

    /**
     * 获取规范路径名，去除路径中的".." "." "//"等重复字符，获取纯净路径名
     */
    @NonNull
    public static String getCanonicalPathname(String pathname) {
        pathname = StringUtils.trim(pathname);
        if (StringUtils.isEmpty(pathname)) {
            return SEPARATOR;
        }

        List<String> cleanNameList = ListUtils.newArrayList();

        String[] names = StringUtils.split(pathname, SEPARATOR_CHAR);
        for (String name : names) {
            if (StringUtils.equals(name, "..")) {
                if (cleanNameList.size() > 0) {
                    cleanNameList.remove(cleanNameList.size() - 1);
                }
            } else if (StringUtils.isNotEmpty(name) && !StringUtils.equals(name, ".")) {
                cleanNameList.add(name);
            }
        }

        String cleanPathname = SEPARATOR + StringUtils.join(cleanNameList, SEPARATOR);
        if (pathname.endsWith(SEPARATOR)) {
            cleanPathname = cleanPathname + SEPARATOR;
        }
        return cleanPathname;
    }

    public static void main(String[] argv) {
        System.out.println(getCanonicalPathname("../../../a/b/c/../d/..//e/"));
        System.out.println(getCanonicalPathname("/../../../a/b/c/d/../e"));
    }

}
