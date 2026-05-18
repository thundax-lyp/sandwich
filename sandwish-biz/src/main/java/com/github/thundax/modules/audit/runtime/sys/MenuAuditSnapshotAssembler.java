package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.modules.audit.entity.valueobject.AuditField;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.runtime.AuditSnapshotAssembler;
import com.github.thundax.modules.audit.runtime.AuditSnapshots;
import com.github.thundax.modules.sys.entity.Menu;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MenuAuditSnapshotAssembler implements AuditSnapshotAssembler {

    @Override
    public String objectType() {
        return "Menu";
    }

    @Override
    public String objectTypeLabel() {
        return "菜单";
    }

    @Override
    public List<AuditField> fields() {
        return Arrays.asList(
                AuditSnapshots.field("name", "名称", null),
                AuditSnapshots.field("perms", "权限", null),
                AuditSnapshots.field("visibility", "可见性", null));
    }

    @Override
    public AuditSnapshot assemble(Object object) {
        Menu menu = (Menu) object;
        if (menu == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                menu.getId(),
                menu.getName(),
                AuditSnapshots.field("name", "名称", menu.getName()),
                AuditSnapshots.field("perms", "权限", menu.getPerms()),
                AuditSnapshots.field("visibility", "可见性", menu.getVisibility()));
    }
}
