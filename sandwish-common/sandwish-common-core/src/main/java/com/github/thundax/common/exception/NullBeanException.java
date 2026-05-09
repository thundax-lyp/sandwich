package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;
import com.github.thundax.common.id.BaseLongId;

public class NullBeanException extends ApiException {

    public NullBeanException(String name, BaseLongId id) {
        super(I18nMessages.getMessage("common.exception.null-bean", name, id));
    }
}
