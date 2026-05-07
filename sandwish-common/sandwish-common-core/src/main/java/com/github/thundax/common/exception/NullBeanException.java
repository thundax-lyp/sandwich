package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;
import com.github.thundax.common.id.EntityId;

public class NullBeanException extends ApiException {

    public NullBeanException(String name, EntityId id) {
        super(I18nMessages.getMessage("common.exception.null-bean", name, id));
    }
}
