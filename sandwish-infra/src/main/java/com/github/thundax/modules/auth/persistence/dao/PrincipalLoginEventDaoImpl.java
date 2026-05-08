package com.github.thundax.modules.auth.persistence.dao;

import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.auth.codec.PrincipalLoginEventIdCodec;
import com.github.thundax.modules.auth.dao.PrincipalLoginEventDao;
import com.github.thundax.modules.auth.entity.PrincipalLoginEvent;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalLoginEventId;
import com.github.thundax.modules.auth.persistence.assembler.PrincipalLoginEventPersistenceAssembler;
import com.github.thundax.modules.auth.persistence.dataobject.PrincipalLoginEventDO;
import com.github.thundax.modules.auth.persistence.mapper.PrincipalLoginEventMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PrincipalLoginEventDaoImpl implements PrincipalLoginEventDao {

    private final PrincipalLoginEventMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public PrincipalLoginEventDaoImpl(PrincipalLoginEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PrincipalLoginEvent getById(PrincipalLoginEventId id) {
        return PrincipalLoginEventPersistenceAssembler.toEntity(
                mapper.selectById(PrincipalLoginEventIdCodec.toValue(id)));
    }

    @Override
    public PrincipalLoginEventId insert(PrincipalLoginEvent event) {
        PrincipalLoginEventDO dataObject = PrincipalLoginEventPersistenceAssembler.toDataObject(event);
        dataObject.setId(PrincipalLoginEventIdCodec.toValue(PrincipalLoginEventIdCodec.nextId(idGenerator)));
        mapper.insert(dataObject);
        return PrincipalLoginEventIdCodec.toDomain(dataObject.getId());
    }
}
