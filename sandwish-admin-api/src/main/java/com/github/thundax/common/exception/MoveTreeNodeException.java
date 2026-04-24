package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

/**
 * @author thundax
 */
public class MoveTreeNodeException extends ApiException {

    public MoveTreeNodeException(String name, String fromId, String toId) {
        super(I18nMessages.getMessage("common.exception.move-tree-node", name, fromId, toId));
    }

}
