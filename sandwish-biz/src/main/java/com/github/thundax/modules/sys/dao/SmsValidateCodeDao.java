package com.github.thundax.modules.sys.dao;

/** 短信验证码发送限流状态 DAO。 */
public interface SmsValidateCodeDao {

    boolean canSend(String mobile);

    void markSent(String mobile, int expiredSeconds);
}
