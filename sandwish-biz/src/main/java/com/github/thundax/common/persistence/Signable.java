package com.github.thundax.common.persistence;

/**
 * 接口：签名对象
 *
 * @author wdit
 */
public interface Signable {

    /**
     * 获取签名对象名称
     *
     * @return sign name
     */
    default String getSignName() {
        return null;
    }

    /**
     * 获取签名对象ID
     *
     * @return sign uuid
     */
    default String getSignId() {
        return null;
    }

    /**
     * 获取签名对象主体
     *
     * @return sign body
     */
    default String getSignBody() {
        return null;
    }
}
