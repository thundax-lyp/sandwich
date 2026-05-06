package com.github.thundax.modules.member.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import com.github.thundax.common.exception.BizException;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import org.junit.Test;

public class MemberPersistenceAssemblerTest {

    @Test
    public void shouldReadEnableFlagAsDomainValue() {
        MemberDO dataObject = new MemberDO();
        dataObject.setEnableFlag("ENABLED");

        Member entity = MemberPersistenceAssembler.toEntity(dataObject);

        assertSame(MemberStatus.ENABLED, entity.getStatus());
    }

    @Test
    public void shouldRejectLegacyEnableFlagValue() {
        MemberDO dataObject = new MemberDO();
        dataObject.setEnableFlag("1");

        try {
            MemberPersistenceAssembler.toEntity(dataObject);
            fail("Legacy enable flag value must be rejected");
        } catch (BizException expected) {
            assertEquals("Unknown member status: 1", expected.getMessage());
        }
    }

    @Test
    public void shouldWriteDomainValueToEnableFlag() {
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
