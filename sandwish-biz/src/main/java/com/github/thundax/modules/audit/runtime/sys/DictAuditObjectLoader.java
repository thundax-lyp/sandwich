package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.sys.service.DictService;
import com.github.thundax.modules.sys.service.query.DictQuery;
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
        DictQuery query = new DictQuery();
        query.setId(EntityIdCodec.toDomain(Long.valueOf(objectId)));
        return dictService.get(query);
    }
}
