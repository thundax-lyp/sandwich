package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

public class InvalidTokenException extends ApiException {

    public InvalidTokenException() {
        super(I18nMessages.getMessage("common.exception.invalid-token"));
    }
}
