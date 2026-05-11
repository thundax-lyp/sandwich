package com.github.thundax.common.exception;

public class ApiException extends Exception {

    private final int code;
    private final String messageKey;
    private final Object[] messageArgs;

    public ApiException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
        this.messageKey = null;
        this.messageArgs = new Object[0];
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
        this.messageKey = null;
        this.messageArgs = new Object[0];
    }

    public ApiException(String messageKey, String defaultMessage, Object... messageArgs) {
        super(defaultMessage);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
        this.messageKey = messageKey;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
    }

    public ApiException(int code, String messageKey, String defaultMessage, Object... messageArgs) {
        super(defaultMessage);
        this.code = code;
        this.messageKey = messageKey;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
    }

    public int getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }
}
