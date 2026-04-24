package com.github.thundax.modules.auth.exception;


import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * MultiMobileException
 *
 * @author wdit
 */
public class MultiMobileException extends ApiException {

    private static final long serialVersionUID = 1L;

    public MultiMobileException() {
        super(I18nMessages.getMessage("auth.exception.multi-mobile"));
    }

}
