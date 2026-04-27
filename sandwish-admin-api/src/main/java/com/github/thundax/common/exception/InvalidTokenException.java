package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

/** @author wdit */
public class InvalidTokenException extends ApiException {

    private static final long serialVersionUID = 1L;

    public InvalidTokenException() {
        super(I18nMessages.getMessage("common.exception.invalid-token"));
    }
}
