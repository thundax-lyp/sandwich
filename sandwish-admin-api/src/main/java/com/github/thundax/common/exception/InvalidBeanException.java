package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

public class InvalidBeanException extends ApiException {

    public InvalidBeanException(String name, String id) {
        super(I18nMessages.getMessage("common.exception.invalid-bean", name, id));
    }
}
