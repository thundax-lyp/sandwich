package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * MultiMobileException
 *
 * @author wdit
 */
public class MultiMobileException extends ApiException {

    public MultiMobileException() {
        super(I18nMessages.getMessage("auth.exception.multi-mobile"));
    }
}
