package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

public class InvalidPasswordException extends ApiException {

    public InvalidPasswordException() {
        super(I18nMessages.getMessage("auth.exception.invalid-password"));
    }
}
