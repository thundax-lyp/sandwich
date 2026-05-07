package com.github.thundax.modules.member.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.member.dao.MemberCredentialDao;
import com.github.thundax.modules.member.dao.MemberIdentityDao;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.entity.MemberCredential;
import com.github.thundax.modules.member.entity.MemberIdentity;
import com.github.thundax.modules.member.entity.enums.MemberCredentialStatus;
import com.github.thundax.modules.member.entity.enums.MemberCredentialType;
import com.github.thundax.modules.member.entity.enums.MemberIdentityStatus;
import com.github.thundax.modules.member.entity.enums.MemberIdentityType;
import java.util.List;
import org.junit.Test;

public class MemberCredentialServiceImplTest {

    @Test
    public void shouldGetPasswordCredentialByAccountIdentity() {
        RecordingMemberIdentityDao identityDao = new RecordingMemberIdentityDao();
        RecordingMemberCredentialDao credentialDao = new RecordingMemberCredentialDao();
        MemberIdentity identity = new MemberIdentity();
        identity.setId(EntityId.of(3001L));
        identityDao.memberTypeResult = identity;
        MemberCredential credential = new MemberCredential();
        credentialDao.identityTypeResult = credential;
        MemberCredentialServiceImpl service = new MemberCredentialServiceImpl(identityDao, credentialDao);

        assertSame(credential, service.getPasswordCredential(EntityId.of(1001L)));
        assertEquals(EntityId.of(3001L), credentialDao.identityId);
        assertEquals(MemberCredentialType.PASSWORD, credentialDao.credentialType);
    }

    @Test
    public void shouldReturnNullWhenAccountIdentityMissing() {
        RecordingMemberIdentityDao identityDao = new RecordingMemberIdentityDao();
        RecordingMemberCredentialDao credentialDao = new RecordingMemberCredentialDao();
        MemberCredentialServiceImpl service = new MemberCredentialServiceImpl(identityDao, credentialDao);

        assertNull(service.getPasswordCredential(EntityId.of(1001L)));
        assertEquals(0, credentialDao.identityTypeCalls);
    }

    @Test
    public void shouldCreatePasswordCredentialWhenMissing() {
        RecordingMemberCredentialDao credentialDao = new RecordingMemberCredentialDao();
        MemberCredentialServiceImpl service =
                new MemberCredentialServiceImpl(new RecordingMemberIdentityDao(), credentialDao);
        Member member = member(1001L);
        MemberIdentity identity = identity(3001L);

        service.upsertPassword(member, identity, "hash");

        assertEquals(EntityId.of(9001L), credentialDao.inserted.getId());
        assertEquals(EntityId.of(1001L), credentialDao.inserted.getMemberId());
        assertEquals(EntityId.of(3001L), credentialDao.inserted.getIdentityId());
        assertEquals(MemberCredentialType.PASSWORD, credentialDao.inserted.getCredentialType());
        assertEquals("hash", credentialDao.inserted.getCredentialValue());
        assertEquals(MemberCredentialStatus.ACTIVE, credentialDao.inserted.getStatus());
    }

    @Test
    public void shouldUpdateExistingPasswordCredential() {
        RecordingMemberCredentialDao credentialDao = new RecordingMemberCredentialDao();
        MemberCredential existing = new MemberCredential();
        existing.setFailedCount(3);
        existing.setStatus(MemberCredentialStatus.LOCKED);
        credentialDao.identityTypeResult = existing;
        MemberCredentialServiceImpl service =
                new MemberCredentialServiceImpl(new RecordingMemberIdentityDao(), credentialDao);

        service.upsertPassword(member(1001L), identity(3001L), "new-hash");

        assertSame(existing, credentialDao.updated);
        assertEquals("new-hash", existing.getCredentialValue());
        assertEquals(0, existing.getFailedCount());
        assertEquals(MemberCredentialStatus.ACTIVE, existing.getStatus());
    }

    private static Member member(Long id) {
        Member member = new Member();
        member.setId(EntityId.of(id));
        return member;
    }

    private static MemberIdentity identity(Long id) {
        MemberIdentity identity = new MemberIdentity();
        identity.setId(EntityId.of(id));
        return identity;
    }

    private static class RecordingMemberIdentityDao implements MemberIdentityDao {
        private MemberIdentity memberTypeResult;

        @Override
        public MemberIdentity getById(EntityId id) {
            return null;
        }

        @Override
        public MemberIdentity getByIdentity(MemberIdentityType identityType, String identityValue) {
            return null;
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
            return null;
        }

        @Override
        public int update(MemberIdentity memberIdentity) {
            return 0;
        }

        @Override
        public int updateStatus(MemberIdentity memberIdentity) {
            return 0;
        }
    }

    private static class RecordingMemberCredentialDao implements MemberCredentialDao {
        private MemberCredential identityTypeResult;
        private MemberCredential inserted;
        private MemberCredential updated;
        private EntityId identityId;
        private MemberCredentialType credentialType;
        private int identityTypeCalls;

        @Override
        public MemberCredential getById(EntityId id) {
            return null;
        }

        @Override
        public MemberCredential getByIdentityIdAndType(EntityId identityId, MemberCredentialType credentialType) {
            this.identityTypeCalls++;
            this.identityId = identityId;
            this.credentialType = credentialType;
            return identityTypeResult;
        }

        @Override
        public MemberCredential getByMemberIdAndType(EntityId memberId, MemberCredentialType credentialType) {
            return null;
        }

        @Override
        public List<MemberCredential> listByMemberIdAndStatus(EntityId memberId, MemberCredentialStatus status) {
            return null;
        }

        @Override
        public EntityId insert(MemberCredential memberCredential) {
            this.inserted = memberCredential;
            memberCredential.setId(EntityId.of(9001L));
            return EntityId.of(9001L);
        }

        @Override
        public int update(MemberCredential memberCredential) {
            this.updated = memberCredential;
            return 1;
        }

        @Override
        public int updateStatus(MemberCredential memberCredential) {
            return 1;
        }

        @Override
        public int updateVerifyState(MemberCredential memberCredential) {
            return 1;
        }
    }
}
