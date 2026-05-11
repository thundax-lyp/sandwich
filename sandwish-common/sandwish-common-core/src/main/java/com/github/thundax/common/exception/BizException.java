package com.github.thundax.common.exception;

public class BizException extends RuntimeException {

    private final String code;
    private final String messageKey;
    private final String defaultMessage;
    private final Object[] messageArgs;

    public BizException(String message) {
        super(message);
        this.code = null;
        this.messageKey = null;
        this.defaultMessage = message;
        this.messageArgs = new Object[0];
    }

    public BizException(String code, String messageKey, String defaultMessage, Object... messageArgs) {
        super(defaultMessage);
        this.code = code;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
    }

    public BizException(String code, String messageKey, String defaultMessage, Throwable cause) {
        super(defaultMessage, cause);
        this.code = code;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
        this.messageArgs = new Object[0];
    }

    public BizException(String code, String messageKey, String defaultMessage, Throwable cause, Object... messageArgs) {
        super(defaultMessage, cause);
        this.code = code;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
    }

    public String getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }
}
