package com.github.thundax.modules.auth.service.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

public final class CaptchaWhitelistProperties {

    public static final String ENABLED_PROPERTY = "sandwish.integration-test.enabled";
    public static final String WHITELIST_VALUES_PROPERTY = "sandwish.integration-test.captcha.whitelist-values";
    public static final String INTEGRATION_PROFILE = "it";

    private final boolean enabled;
    private final Set<String> whitelistValues;

    private CaptchaWhitelistProperties(boolean enabled, Collection<String> whitelistValues) {
        this.enabled = enabled;
        this.whitelistValues = normalize(whitelistValues);
    }

    public static CaptchaWhitelistProperties from(Environment environment) {
        boolean enabled = environment != null
                && environment.acceptsProfiles(INTEGRATION_PROFILE)
                && Boolean.parseBoolean(environment.getProperty(ENABLED_PROPERTY, "false"));
        String values = environment == null ? StringUtils.EMPTY : environment.getProperty(WHITELIST_VALUES_PROPERTY);
        String[] splitValues = StringUtils.split(values, ',');
        return new CaptchaWhitelistProperties(
                enabled, splitValues == null ? Collections.emptyList() : Arrays.asList(splitValues));
    }

    public static CaptchaWhitelistProperties of(boolean enabled, Collection<String> whitelistValues) {
        return new CaptchaWhitelistProperties(enabled, whitelistValues);
    }

    public static CaptchaWhitelistProperties disabled() {
        return new CaptchaWhitelistProperties(false, Collections.emptyList());
    }

    public boolean matches(String captcha) {
        return enabled && whitelistValues.contains(StringUtils.trimToEmpty(captcha));
    }

    public Set<String> getWhitelistValues() {
        return Collections.unmodifiableSet(whitelistValues);
    }

    private static Set<String> normalize(Collection<String> whitelistValues) {
        Set<String> values = new LinkedHashSet<>();
        if (whitelistValues == null) {
            return values;
        }
        for (String whitelistValue : whitelistValues) {
            String value = StringUtils.trimToEmpty(whitelistValue);
            if (StringUtils.isNotBlank(value)) {
                values.add(value);
            }
        }
        return values;
    }
}
