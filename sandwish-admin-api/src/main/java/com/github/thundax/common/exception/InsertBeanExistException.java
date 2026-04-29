package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

public class InsertBeanExistException extends ApiException {

    public InsertBeanExistException(String name, String id) {
        super(I18nMessages.getMessage("common.exception.insert-bean-exist", name, id));
    }
}
