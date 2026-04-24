package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

/**
 * @author wdit
 */
public class NotLocalBeanException extends ApiException {

    public NotLocalBeanException(String name, String id) {
        super(I18nMessages.getMessage("common.exception.not-local-bean", name, id));
    }

}
