package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

/** @author wdit */
public class NullBeanException extends ApiException {

    public NullBeanException(String name, String id) {
        super(I18nMessages.getMessage("common.exception.null-bean", name, id));
    }
}
