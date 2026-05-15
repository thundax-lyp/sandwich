package com.github.thundax.modules.submission.entity.enums;

import com.github.thundax.common.exception.DomainException;
import java.util.Arrays;

public enum SubmissionStatus {
    SUBMITTED,
    APPROVED,
    REJECTED,
    CLOSED;

    public String value() {
        return name();
    }

    public static SubmissionStatus from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "SUBMISSION-90001", "submission.domain.status.invalid", "Unknown submission status: " + value));
    }
}
