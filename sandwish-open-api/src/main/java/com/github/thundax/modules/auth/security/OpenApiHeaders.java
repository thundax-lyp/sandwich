package com.github.thundax.modules.auth.security;

public final class OpenApiHeaders {

    public static final String API_KEY = "X-Sandwish-Api-Key";
    public static final String TIMESTAMP = "X-Sandwish-Timestamp";
    public static final String NONCE = "X-Sandwish-Nonce";
    public static final String CONTENT_SHA256 = "X-Sandwish-Content-SHA256";
    public static final String SIGNATURE = "X-Sandwish-Signature";

    private OpenApiHeaders() {}
}
