package com.github.thundax.modules.member.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.config.Global;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import org.junit.Test;

public class MemberPersistenceAssemblerTest {

    @Test
    public void shouldReadLegacyEnableFlagAsDomainValue() {
        MemberDO dataObject = new MemberDO();
        dataObject.setEnableFlag(Global.ENABLE);

        Member entity = MemberPersistenceAssembler.toEntity(dataObject);

        assertSame(MemberStatus.ENABLED, entity.getStatus());
    }

    @Test
    public void shouldWriteDomainValueToLegacyEnableFlag() {
        Member entity = new Member();
        entity.setStatus(MemberStatus.DISABLED);

        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(entity);

        assertEquals("DISABLED", dataObject.getEnableFlag());
    }
}
