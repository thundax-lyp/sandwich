package com.github.thundax.common.exception;

import com.github.thundax.common.id.BaseLongId;

public class MoveTreeNodeException extends ApiException {

    public MoveTreeNodeException(String name, BaseLongId fromId, BaseLongId toId) {
        super("can not move " + name + " node from " + fromId + " to " + toId);
    }
}
