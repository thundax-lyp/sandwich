package com.github.thundax.common.exception;

public class NestedTreeNodeException extends ApiException {

    public NestedTreeNodeException(String name, String id1, String id2) {
        super("nested " + name + " tree node, id: " + id1 + ", " + id2);
    }
}
