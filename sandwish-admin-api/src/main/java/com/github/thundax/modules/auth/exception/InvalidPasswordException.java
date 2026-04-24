package com.github.thundax.modules.auth.exception;


import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * InvalidPasswordException
 *
 * @author wdit
 */
public class InvalidPasswordException extends ApiException {

    private static final long serialVersionUID = 1L;

    public InvalidPasswordException() {
        super(I18nMessages.getMessage("auth.exception.invalid-password"));
    }

}
