package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

/** @author wdit */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException() {
        super(I18nMessages.getMessage("common.exception.unauthorized"));
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
