package com.github.thundax.modules.sys.ueditor.define;

/**
 * 处理状态接口
 *
 * @author hancong03@baidu.com
 */
public interface State {

    /**
     * 是否成功
     *
     * @return 成功:true
     */
    boolean isSuccess();

    /**
     * 设置数据
     *
     * @param name 名称
     * @param val  数值
     */
    void putInfo(String name, String val);

    /**
     * 设置数据
     *
     * @param name 名称
     * @param val  数值
     */
    void putInfo(String name, long val);

    /**
     * JSON字符串
     *
     * @return JSON字符串
     */
    String toJson();

}
