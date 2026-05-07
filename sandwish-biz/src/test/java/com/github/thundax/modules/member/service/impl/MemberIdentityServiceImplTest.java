package com.github.thundax.modules.member.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.dao.MemberIdentityDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberIdentityStatus;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import java.util.List;
import org.junit.Test;

public class MemberIdentityServiceImplTest {

    @Test
    public void shouldGetIdentityBySupportedTypes() {
        RecordingMemberIdentityDao dao = new RecordingMemberIdentityDao();
        MemberIdentity expected = new MemberIdentity();
        dao.identityResult = expected;
        MemberIdentityServiceImpl service = new MemberIdentityServiceImpl(dao);

        assertSame(expected, service.getByAccount("alice"));
        assertEquals(MemberIdentityType.ACCOUNT, dao.identityType);
        assertEquals("alice", dao.identityValue);

        service.getByMobile("13800138000");
        assertEquals(MemberIdentityType.MOBILE, dao.identityType);

        service.getByEmail("a@example.com");
        assertEquals(MemberIdentityType.EMAIL, dao.identityType);
    }

    @Test
    public void shouldIgnoreBlankIdentityValue() {
        RecordingMemberIdentityDao dao = new RecordingMemberIdentityDao();
        MemberIdentityServiceImpl service = new MemberIdentityServiceImpl(dao);

        assertNull(service.getByIdentity(MemberIdentityType.ACCOUNT, " "));
        assertEquals(0, dao.identityCalls);
    }

    @Test
    public void shouldCreateIdentityWhenMissing() {
        RecordingMemberIdentityDao dao = new RecordingMemberIdentityDao();
        Member member = new Member();
        member.setId(EntityId.of(1001L));
        MemberIdentityServiceImpl service = new MemberIdentityServiceImpl(dao);

        MemberIdentity identity = service.updateIdentity(member, MemberIdentityType.ACCOUNT, "alice");

        assertEquals(EntityId.of(9001L), identity.getId());
        assertEquals(EntityId.of(1001L), identity.getMemberId());
        assertEquals(MemberIdentityType.ACCOUNT, identity.getIdentityType());
        assertEquals("alice", identity.getIdentityValue());
        assertEquals(MemberIdentityStatus.ENABLED, identity.getStatus());
        assertSame(identity, dao.inserted);
    }

    @Test
    public void shouldUpdateExistingIdentity() {
        RecordingMemberIdentityDao dao = new RecordingMemberIdentityDao();
        Member member = new Member();
        member.setId(EntityId.of(1001L));
        MemberIdentity existing = new MemberIdentity();
        existing.setId(EntityId.of(9001L));
        existing.setStatus(MemberIdentityStatus.DISABLED);
        dao.memberTypeResult = existing;
        MemberIdentityServiceImpl service = new MemberIdentityServiceImpl(dao);

        MemberIdentity identity = service.updateIdentity(member, MemberIdentityType.EMAIL, "a@example.com");

        assertSame(existing, identity);
        assertEquals("a@example.com", existing.getIdentityValue());
        assertEquals(MemberIdentityStatus.ENABLED, existing.getStatus());
        assertSame(existing, dao.updated);
    }

    private static class RecordingMemberIdentityDao implements MemberIdentityDao {
        private MemberIdentity identityResult;
        private MemberIdentity memberTypeResult;
        private MemberIdentity inserted;
        private MemberIdentity updated;
        private MemberIdentityType identityType;
        private String identityValue;
        private int identityCalls;

        @Override
        public MemberIdentity getById(EntityId id) {
            return null;
        }

        @Override
        public MemberIdentity getByIdentity(MemberIdentityType identityType, String identityValue) {
            this.identityCalls++;
            this.identityType = identityType;
            this.identityValue = identityValue;
            return identityResult;
        }

        @Override
        public MemberIdentity getByMemberIdAndType(EntityId memberId, MemberIdentityType identityType) {
            return memberTypeResult;
        }

        @Override
        public List<MemberIdentity> listByMemberIdAndStatus(EntityId memberId, MemberIdentityStatus status) {
            return null;
        }

        @Override
        public EntityId insert(MemberIdentity memberIdentity) {
            this.inserted = memberIdentity;
            return EntityId.of(9001L);
        }

        @Override
        public int update(MemberIdentity memberIdentity) {
            this.updated = memberIdentity;
            return 1;
        }

        @Override
        public int updateStatus(MemberIdentity memberIdentity) {
            return 1;
        }
    }
}
