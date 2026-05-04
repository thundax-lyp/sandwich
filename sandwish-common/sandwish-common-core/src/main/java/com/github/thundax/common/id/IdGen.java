package com.github.thundax.common.id;

import java.util.UUID;

/**
 * 封装各种生成唯一性ID算法的工具类.
 */
public final class IdGen {

    private IdGen() {}

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
