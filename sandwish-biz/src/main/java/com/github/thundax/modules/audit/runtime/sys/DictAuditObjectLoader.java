package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.sys.entity.valueobject.DictIdCodec;
import com.github.thundax.modules.sys.service.DictService;
import org.springframework.stereotype.Component;

@Component
public class DictAuditObjectLoader implements AuditObjectLoader {

    private final DictService dictService;

    public DictAuditObjectLoader(DictService dictService) {
        this.dictService = dictService;
    }

    @Override
    public String objectType() {
        return "Dict";
    }

    @Override
    public Object load(String objectId) {
        return dictService.get(DictIdCodec.toDomain(Long.valueOf(objectId)));
    }
}
