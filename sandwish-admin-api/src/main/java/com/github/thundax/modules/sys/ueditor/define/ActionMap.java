package com.github.thundax.modules.sys.ueditor.define;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义请求action类型
 *
 * @author hancong03@baidu.com
 *
 */
@SuppressWarnings("serial")
public final class ActionMap {

    public static final Map<String, Integer> MAPPING;

    /**
     * 获取配置请求
     */
    public static final int CONFIG = 0;
    public static final int UPLOAD_IMAGE = 1;

    static {
        MAPPING = new HashMap<String, Integer>() {
            {
                put("config", ActionMap.CONFIG);
                put("uploadimage", ActionMap.UPLOAD_IMAGE);
            }
        };
    }

    public static int getType(String key) {
        return ActionMap.MAPPING.get(key);
    }

}
