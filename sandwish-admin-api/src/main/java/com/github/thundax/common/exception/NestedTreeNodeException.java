package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

/**
 * @author wdit
 */
public class NestedTreeNodeException extends ApiException {

    public NestedTreeNodeException(String name, String id1, String id2) {
        super(I18nMessages.getMessage("common.exception.nested-tree-node", name, id1, id2));
    }

}
