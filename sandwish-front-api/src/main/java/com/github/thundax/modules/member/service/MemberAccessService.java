package com.github.thundax.modules.member.service;

public interface MemberAccessService {

    /**
     * 获取用户id
     *
     * @return 用户id
     */
    String getCurrentMemberId();

    /**
     * 获取session缓存对象
     *
     * @param name 名称
     * @return 对象
     */
    Object getSessionCache(String name);

    /**
     * 设置session缓存对象
     *
     * @param name 名称
     * @param value 对象
     */
    void setSessionCache(String name, Object value);
}
