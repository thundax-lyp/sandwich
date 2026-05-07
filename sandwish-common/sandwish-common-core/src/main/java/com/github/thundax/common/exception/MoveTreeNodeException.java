package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;
import com.github.thundax.common.id.EntityId;

public class MoveTreeNodeException extends ApiException {

    public MoveTreeNodeException(String name, EntityId fromId, EntityId toId) {
        super(I18nMessages.getMessage("common.exception.move-tree-node", name, fromId, toId));
    }
}
