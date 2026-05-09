package com.github.thundax.modules.audit.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AuditSnapshotAssemblerRegistry {

    private final Map<String, AuditSnapshotAssembler> assemblers = new HashMap<>();

    public AuditSnapshotAssemblerRegistry(List<AuditSnapshotAssembler> assemblerList) {
        if (assemblerList != null) {
            for (AuditSnapshotAssembler assembler : assemblerList) {
                assemblers.put(assembler.objectType(), assembler);
            }
        }
    }

    public AuditSnapshotAssembler get(String objectType) {
        return assemblers.get(objectType);
    }
}
