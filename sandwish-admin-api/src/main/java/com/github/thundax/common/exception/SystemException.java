package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

public class SystemException extends ApiException {

    public SystemException() {
        super(I18nMessages.getMessage("common.exception.system"));
    }

    public SystemException(String message) {
        super(message);
    }
}
