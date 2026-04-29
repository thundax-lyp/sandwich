package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * InvalidUsernamePasswordException
 *
 * @author thundax
 */
public class InvalidLoginCountException extends ApiException {

    public InvalidLoginCountException(String expire, String maxFailCount, String lockTime, String remainCount) {
        super(I18nMessages.getMessage(
                "auth.exception.invalid-login-count", expire, maxFailCount, lockTime, remainCount));
    }
}
