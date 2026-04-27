package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

/** @author thundax */
public class InvalidBeanException extends ApiException {

    private static final long serialVersionUID = 1L;

    public InvalidBeanException(String name, String id) {
        super(I18nMessages.getMessage("common.exception.invalid-bean", name, id));
    }
}
