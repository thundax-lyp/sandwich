package com.github.thundax.common.web.exception;

public class SandwishException extends RuntimeException {

    private final WebErrorCode errorCode;
    private final String code;

    public SandwishException(WebErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }

    public SandwishException(WebErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }

    public SandwishException(WebErrorCode errorCode, String code, String message) {
        super(message);
        this.errorCode = errorCode;
        this.code = code;
    }

    public WebErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
