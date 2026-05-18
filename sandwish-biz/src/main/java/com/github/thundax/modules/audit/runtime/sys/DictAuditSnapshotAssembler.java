package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.modules.audit.entity.valueobject.AuditField;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.runtime.AuditSnapshotAssembler;
import com.github.thundax.modules.audit.runtime.AuditSnapshots;
import com.github.thundax.modules.sys.entity.Dict;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DictAuditSnapshotAssembler implements AuditSnapshotAssembler {

    @Override
    public String objectType() {
        return "Dict";
    }

    @Override
    public String objectTypeLabel() {
        return "字典";
    }

    @Override
    public List<AuditField> fields() {
        return Arrays.asList(
                AuditSnapshots.field("type", "类型", null),
                AuditSnapshots.field("label", "标签", null),
                AuditSnapshots.field("value", "值", null));
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
