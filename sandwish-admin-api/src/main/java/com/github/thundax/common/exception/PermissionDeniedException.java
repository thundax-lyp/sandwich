package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

public class PermissionDeniedException extends ApiException {

    public PermissionDeniedException() {
        super(I18nMessages.getMessage("common.exception.permission-denied"));
    }

    public PermissionDeniedException(String message) {
        super(message);
    }
}
