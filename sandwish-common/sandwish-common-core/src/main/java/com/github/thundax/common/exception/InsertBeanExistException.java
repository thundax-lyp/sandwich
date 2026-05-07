package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;
import com.github.thundax.common.id.EntityId;

public class InsertBeanExistException extends ApiException {

    public InsertBeanExistException(String name, EntityId id) {
        super(I18nMessages.getMessage("common.exception.insert-bean-exist", name, id));
    }
}
