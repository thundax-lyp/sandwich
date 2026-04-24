package com.github.thundax.modules.auth.exception;


import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * TooManyLoginRequestException
 *
 * @author thundax
 */
public class TooManyLoginRequestException extends ApiException {

    private static final long serialVersionUID = 1L;

    public TooManyLoginRequestException() {
        super(I18nMessages.getMessage("auth.exception.too-many-login-request"));
    }

}
