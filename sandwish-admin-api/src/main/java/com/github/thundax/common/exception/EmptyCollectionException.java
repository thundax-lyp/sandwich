package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

public class EmptyCollectionException extends ApiException {

    public EmptyCollectionException() {
        super(I18nMessages.getMessage("common.exception.empty-collection"));
    }
}
