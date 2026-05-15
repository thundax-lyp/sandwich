package com.github.thundax.modules.auth.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class OpenApiIpWhitelistMatcher {

    private static final TypeReference<List<String>> IP_LIST_TYPE = new TypeReference<List<String>>() {};

    private final ObjectMapper objectMapper;

    public OpenApiIpWhitelistMatcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean matches(String whitelistJson, String remoteIp) {
        List<String> whitelist = parseWhitelist(whitelistJson);
        if (whitelist.isEmpty()) {
            return true;
        }
        if (StringUtils.isBlank(remoteIp)) {
            return false;
        }
        for (String rule : whitelist) {
            if (matchesRule(rule, remoteIp)) {
                return true;
            }
        }
        return false;
    }

    private List<String> parseWhitelist(String whitelistJson) {
        if (StringUtils.isBlank(whitelistJson)) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(whitelistJson, IP_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (Exception e) {
            throw new IllegalArgumentException("OpenClient IP whitelist is invalid", e);
        }
    }

    private boolean matchesRule(String rule, String remoteIp) {
        if (StringUtils.isBlank(rule)) {
            return false;
        }
        String normalizedRule = rule.trim();
        if (!normalizedRule.contains("/")) {
            return normalizedRule.equals(remoteIp);
        }
        return matchesCidr(normalizedRule, remoteIp);
    }

    private boolean matchesCidr(String cidr, String remoteIp) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }
        try {
            int prefixLength = Integer.parseInt(parts[1]);
            if (prefixLength < 0 || prefixLength > 32) {
                return false;
            }
            int mask = prefixLength == 0 ? 0 : -1 << (32 - prefixLength);
            return (ipv4ToInt(parts[0]) & mask) == (ipv4ToInt(remoteIp) & mask);
        } catch (Exception e) {
            return false;
        }
    }

    private int ipv4ToInt(String value) throws Exception {
        byte[] address = InetAddress.getByName(value).getAddress();
        if (address.length != 4) {
            throw new IllegalArgumentException("Only IPv4 is supported");
        }
        return ((address[0] & 0xff) << 24)
                | ((address[1] & 0xff) << 16)
                | ((address[2] & 0xff) << 8)
                | (address[3] & 0xff);
    }
}
