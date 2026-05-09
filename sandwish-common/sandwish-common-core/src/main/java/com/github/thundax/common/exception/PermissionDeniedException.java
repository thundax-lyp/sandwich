package com.github.thundax.common.exception;

public class PermissionDeniedException extends ApiException {

    public PermissionDeniedException() {
        super("permission denied");
    }
}
