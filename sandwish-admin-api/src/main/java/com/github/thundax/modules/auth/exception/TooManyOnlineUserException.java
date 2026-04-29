package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * TooManyOnlineUserException
 *
 * @author thundax
 */
public class TooManyOnlineUserException extends ApiException {

    public TooManyOnlineUserException() {
        super(I18nMessages.getMessage("auth.exception.too-many-online-user"));
    }
}
