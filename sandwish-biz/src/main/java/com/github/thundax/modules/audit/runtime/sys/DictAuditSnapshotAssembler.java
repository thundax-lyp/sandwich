package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.runtime.AuditSnapshotAssembler;
import com.github.thundax.modules.audit.runtime.AuditSnapshots;
import com.github.thundax.modules.sys.entity.Dict;
import org.springframework.stereotype.Component;

@Component
public class DictAuditSnapshotAssembler implements AuditSnapshotAssembler {

    @Override
    public String objectType() {
        return "Dict";
    }

    @Override
    public AuditSnapshot assemble(Object object) {
        Dict dict = (Dict) object;
        if (dict == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                dict.getId(),
                dict.getLabel(),
                AuditSnapshots.field("type", "类型", dict.getType()),
                AuditSnapshots.field("label", "标签", dict.getLabel()),
                AuditSnapshots.field("value", "值", dict.getValue()));
    }
}
