package com.github.thundax.modules.member.persistence.assembler;

import static org.junit.Assert.*;

import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.enums.MemberGender;
import com.github.thundax.modules.member.entity.enums.MemberStatus;
import com.github.thundax.modules.member.entity.valueobject.MemberId;
import com.github.thundax.modules.member.persistence.dataobject.MemberDO;
import org.junit.Test;

public class MemberPersistenceAssemblerTest {

    @Test
    public void shouldReadStatusAsDomainValue() {
        MemberDO dataObject = new MemberDO();
        dataObject.setStatus("ACTIVE");

        Member entity = MemberPersistenceAssembler.toEntity(dataObject);

        assertSame(MemberStatus.ACTIVE, entity.getStatus());
    }

    @Test
    public void shouldRejectLegacyStatusValue() {
        MemberDO dataObject = new MemberDO();
        dataObject.setStatus("1");

        try {
            MemberPersistenceAssembler.toEntity(dataObject);
            fail("Legacy status value must be rejected");
        } catch (RuntimeException expected) {
            assertEquals("Unknown member status: 1", expected.getMessage());
        }
    }

    @Test
    public void shouldWriteDomainValueToStatus() {
        Member entity = new Member();
        entity.setId(MemberId.of(5001L));
        entity.setStatus(MemberStatus.SUSPENDED);

        MemberDO dataObject = MemberPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(5001L), dataObject.getId());
        assertEquals("SUSPENDED", dataObject.getStatus());
    }

    @Test
    public void shouldMapGenderAtPersistenceBoundary() {
        MemberDO dataObject = new MemberDO();
        dataObject.setGender("PRIVATE");

        Member entity = MemberPersistenceAssembler.toEntity(dataObject);

        assertSame(MemberGender.PRIVATE, entity.getGender());
        entity.setGender(MemberGender.FEMALE);
        assertEquals("FEMALE", MemberPersistenceAssembler.toDataObject(entity).getGender());
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
