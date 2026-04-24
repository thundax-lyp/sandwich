package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

/**
 * @author wdit
 */
public class InsertBeanExistException extends ApiException {

    private static final long serialVersionUID = 1L;

    public InsertBeanExistException(String name, String id) {
        super(I18nMessages.getMessage("common.exception.insert-bean-exist", name, id));
    }

}
