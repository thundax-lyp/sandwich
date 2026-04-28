package com.github.thundax.modules.sys.dao;

public interface SmsValidateCodeDao {

    boolean canSend(String mobile);

    void markSent(String mobile, int expiredSeconds);
}
