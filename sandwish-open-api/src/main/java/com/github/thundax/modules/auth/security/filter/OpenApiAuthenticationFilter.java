package com.github.thundax.modules.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.common.exception.OpenApiResponseExceptions;
import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import com.github.thundax.common.security.context.SandwishSubjectType;
import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.security.OpenApiCanonicalRequest;
import com.github.thundax.modules.auth.security.OpenApiHeaders;
import com.github.thundax.modules.auth.security.OpenApiIpWhitelistMatcher;
import com.github.thundax.modules.auth.security.OpenApiNonceStore;
import com.github.thundax.modules.auth.security.OpenApiPermissionChecker;
import com.github.thundax.modules.auth.security.OpenApiSignatureVerifier;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.query.PrincipalCredentialQuery;
import com.github.thundax.modules.auth.service.query.PrincipalIdentityQuery;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import com.github.thundax.modules.open.service.OpenClientService;
import com.github.thundax.modules.open.service.dto.OpenClientDTO;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class OpenApiAuthenticationFilter extends OncePerRequestFilter {

    private static final String APPLICATION_JSON_UTF8_VALUE = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8";
    private static final long TIMESTAMP_WINDOW_MILLIS = 5 * 60 * 1000L;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final PrincipalIdentityService principalIdentityService;
    private final PrincipalCredentialService principalCredentialService;
    private final OpenClientService openClientService;
    private final OpenApiSignatureVerifier signatureVerifier;
    private final OpenApiNonceStore nonceStore;
    private final OpenApiIpWhitelistMatcher ipWhitelistMatcher;
    private final OpenApiPermissionChecker permissionChecker;
    private final StringEncryptor stringEncryptor;
    private final ObjectMapper objectMapper;

    public OpenApiAuthenticationFilter(
            PrincipalIdentityService principalIdentityService,
            PrincipalCredentialService principalCredentialService,
            OpenClientService openClientService,
            OpenApiSignatureVerifier signatureVerifier,
            OpenApiNonceStore nonceStore,
            OpenApiIpWhitelistMatcher ipWhitelistMatcher,
            OpenApiPermissionChecker permissionChecker,
            StringEncryptor stringEncryptor,
            ObjectMapper objectMapper) {
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
        this.openClientService = openClientService;
        this.signatureVerifier = signatureVerifier;
        this.nonceStore = nonceStore;
        this.ipWhitelistMatcher = ipWhitelistMatcher;
        this.permissionChecker = permissionChecker;
        this.stringEncryptor = stringEncryptor;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri =
                request.getRequestURI().substring(request.getContextPath().length());
        return !pathMatcher.match("/api/**", requestUri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        try {
            authenticate(cachedRequest);
            filterChain.doFilter(cachedRequest, response);
        } catch (SandwishException exception) {
            SandwishContextHolder.clearSubject();
            writeError(response, exception);
        } finally {
            SandwishContextHolder.clearSubject();
        }
    }

    private void authenticate(CachedBodyHttpServletRequest request) {
        String apiKey = request.getHeader(OpenApiHeaders.API_KEY);
        if (StringUtils.isBlank(apiKey)) {
            throw OpenApiResponseExceptions.missingApiKey();
        }

        PrincipalIdentity identity = principalIdentityService.get(identityQuery(apiKey));
        PrincipalKey principalKey = identity == null ? null : identity.getPrincipalKey();
        if (identity == null || !identity.isEnabled() || principalKey == null || !principalKey.isOpenClient()) {
            throw OpenApiResponseExceptions.invalidApiKey();
        }

        OpenClientDTO client = openClientService.get(OpenClientId.of(principalKey.getPrincipalId()));
        if (client == null
                || client.getId() == null
                || client.getStatus() == null
                || !"ENABLED".equals(client.getStatus().value())) {
            throw OpenApiResponseExceptions.openClientUnavailable();
        }
        if (client.getExpiredAt() != null && !client.getExpiredAt().after(new Date())) {
            throw OpenApiResponseExceptions.openClientUnavailable();
        }
        if (!ipWhitelistMatcher.matches(client.getIpWhitelist(), clientIp(request))) {
            throw OpenApiResponseExceptions.ipNotAllowed();
        }

        PrincipalCredential credential = principalCredentialService.get(credentialQuery(identity));
        if (credential == null || !credential.isActive() || StringUtils.isBlank(credential.getCredentialValue())) {
            throw OpenApiResponseExceptions.apiSecretNotConfigured();
        }
        if (credential.isExpired(new Date()) || credential.isLocked(new Date())) {
            throw OpenApiResponseExceptions.apiSecretNotConfigured();
        }

        validateTimestamp(request.getHeader(OpenApiHeaders.TIMESTAMP));
        String nonce = request.getHeader(OpenApiHeaders.NONCE);
        if (!nonceStore.markUsed(apiKey, nonce)) {
            throw OpenApiResponseExceptions.replayedNonce();
        }
        if (!signatureVerifier.matchesBodyHash(
                request.getCachedBody(), request.getHeader(OpenApiHeaders.CONTENT_SHA256))) {
            throw OpenApiResponseExceptions.invalidContentSha256();
        }

        String secret = stringEncryptor.decrypt(credential.getCredentialValue());
        if (!signatureVerifier.verify(
                secret, OpenApiCanonicalRequest.from(request), request.getHeader(OpenApiHeaders.SIGNATURE))) {
            throw OpenApiResponseExceptions.invalidSignature();
        }

        Set<String> permissions = permissionChecker.permissions(client.getId());
        SandwishContextHolder.setSubject(new SandwishSubject(
                String.valueOf(client.getId().value()),
                SandwishSubjectType.OPEN_CLIENT,
                client.getName(),
                apiKey,
                permissions));
    }

    private void validateTimestamp(String value) {
        if (StringUtils.isBlank(value)) {
            throw OpenApiResponseExceptions.invalidTimestamp();
        }
        try {
            long timestamp = Long.parseLong(value);
            long timestampMillis = value.length() <= 10 ? timestamp * 1000L : timestamp;
            if (Math.abs(System.currentTimeMillis() - timestampMillis) > TIMESTAMP_WINDOW_MILLIS) {
                throw OpenApiResponseExceptions.invalidTimestamp();
            }
        } catch (NumberFormatException e) {
            throw OpenApiResponseExceptions.invalidTimestamp();
        }
    }

    private PrincipalIdentityQuery identityQuery(String apiKey) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setIdentityType(PrincipalIdentityType.API_KEY);
        query.setIdentityValue(apiKey);
        return query;
    }

    private PrincipalCredentialQuery credentialQuery(PrincipalIdentity identity) {
        PrincipalCredentialQuery query = new PrincipalCredentialQuery();
        query.setIdentityId(identity.getId());
        query.setCredentialType(PrincipalCredentialType.API_SECRET);
        return query;
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void writeError(HttpServletResponse response, SandwishException exception) throws IOException {
        String jsonString = objectMapper.writeValueAsString(
                ApiResponse.failure(exception.getCode(), exception.getDefaultMessage()));
        response.setStatus(exception.getHttpStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(APPLICATION_JSON_UTF8_VALUE);
        response.getOutputStream().write(jsonString.getBytes(StandardCharsets.UTF_8));
    }

    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        }

        byte[] getCachedBody() {
            return cachedBody.clone();
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(cachedBody);
        }

        @Override
        public BufferedReader getReader() {
            Charset charset = StringUtils.isBlank(getCharacterEncoding())
                    ? StandardCharsets.UTF_8
                    : Charset.forName(getCharacterEncoding());
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody), charset));
        }
    }

    private static class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        CachedBodyServletInputStream(byte[] body) {
            this.inputStream = new ByteArrayInputStream(body == null ? new byte[0] : body);
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() <= 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("Async request body read is not supported");
        }

        @Override
        public int read() {
            return inputStream.read();
        }
    }
}
