package com.github.thundax.modules.audit.runtime;

public interface AuditObjectLoader {

    String objectType();

    Object load(String objectId);
}
