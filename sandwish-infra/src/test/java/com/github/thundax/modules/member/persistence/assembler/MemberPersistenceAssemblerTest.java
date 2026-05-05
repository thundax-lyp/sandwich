package com.github.thundax.modules.member.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import org.junit.Test;

public class MemberPersistenceAssemblerTest {

    private static final String LEGACY_ENABLED = "1";

    @Test
    public void shouldReadLegacyEnableFlagAsDomainValue() {
        MemberDO dataObject = new MemberDO();
        dataObject.setEnableFlag(LEGACY_ENABLED);

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

    @Test
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        Member entity = new Member();
        entity.setPriority(-1);
        MemberDO dataObject = new MemberDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                MemberPersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, MemberPersistenceAssembler.toEntity(dataObject).getPriority());
    }
}
