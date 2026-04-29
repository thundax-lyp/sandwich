package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * InvalidUsernamePasswordException
 *
 * @author thundax
 */
public class InvalidUsernamePasswordException extends ApiException {

    public InvalidUsernamePasswordException() {
        super(I18nMessages.getMessage("auth.exception.invalid-username-password"));
    }
}
