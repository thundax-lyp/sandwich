package com.github.thundax.common.utils;

import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 封装各种生成唯一性ID算法的工具类.
 *
 * @author thundax
 */
@Service
@Lazy(false)
public class IdGen {

    /** 封装JDK自带的UUID, 通过Random数字生成, 中间无-分割. */
    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
