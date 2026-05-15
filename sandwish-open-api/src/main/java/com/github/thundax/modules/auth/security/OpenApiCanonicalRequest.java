package com.github.thundax.modules.auth.security;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

public class OpenApiCanonicalRequest {

    private final String method;
    private final String path;
    private final String queryString;
    private final String timestamp;
    private final String nonce;
    private final String contentSha256;

    public OpenApiCanonicalRequest(
            String method, String path, String queryString, String timestamp, String nonce, String contentSha256) {
        this.method = method;
        this.path = path;
        this.queryString = queryString;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.contentSha256 = contentSha256;
    }

    public static OpenApiCanonicalRequest from(HttpServletRequest request) {
        return new OpenApiCanonicalRequest(
                request.getMethod() == null ? "" : request.getMethod().toUpperCase(),
                request.getRequestURI(),
                normalizeQueryString(request.getQueryString()),
                request.getHeader(OpenApiHeaders.TIMESTAMP),
                request.getHeader(OpenApiHeaders.NONCE),
                request.getHeader(OpenApiHeaders.CONTENT_SHA256));
    }

    public String value() {
        return String.join(
                "\n",
                nullToEmpty(method),
                nullToEmpty(path),
                nullToEmpty(queryString),
                nullToEmpty(timestamp),
                nullToEmpty(nonce),
                nullToEmpty(contentSha256));
    }

    private static String normalizeQueryString(String queryString) {
        if (queryString == null || queryString.trim().isEmpty()) {
            return "";
        }
        return Arrays.stream(queryString.split("&")).sorted().collect(Collectors.joining("&"));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
