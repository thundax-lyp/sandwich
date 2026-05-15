package com.github.thundax.modules.open.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.auth.dao.PrincipalCredentialDao;
import com.github.thundax.modules.auth.dao.PrincipalIdentityDao;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalCredentialId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalIdentityId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.open.dao.OpenClientDao;
import com.github.thundax.modules.open.entity.OpenClient;
import com.github.thundax.modules.open.entity.OpenClientPermission;
import com.github.thundax.modules.open.entity.enums.OpenClientStatus;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import com.github.thundax.modules.open.service.command.ChangeOpenClientStatusCommand;
import com.github.thundax.modules.open.service.command.CreateOpenClientCommand;
import com.github.thundax.modules.open.service.command.ResetOpenClientSecretCommand;
import com.github.thundax.modules.open.service.command.UpdateOpenClientCommand;
import com.github.thundax.modules.open.service.dto.OpenClientDTO;
import com.github.thundax.modules.open.service.query.OpenClientQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class OpenClientServiceImplTest {

    @Test
    public void shouldCreateOpenClientWithApiKeySecretAndPermissions() {
        RecordingOpenClientDao openClientDao = new RecordingOpenClientDao();
        RecordingPrincipalIdentityDao identityDao = new RecordingPrincipalIdentityDao();
        RecordingPrincipalCredentialDao credentialDao = new RecordingPrincipalCredentialDao();
        OpenClientServiceImpl service = new OpenClientServiceImpl(openClientDao, identityDao, credentialDao);

        OpenClientDTO dto = service.create(new CreateOpenClientCommand(
                "third-party", "[\"127.0.0.1\"]", null, "remarks", Arrays.asList("submission:submission:create")));

        assertEquals(OpenClientId.of(9001L), dto.getId());
        assertEquals(OpenClientStatus.ENABLED, dto.getStatus());
        assertTrue(dto.getApiKey().startsWith("swak_"));
        assertTrue(dto.getApiSecret().startsWith("swas_"));
        assertEquals(Arrays.asList("submission:submission:create"), dto.getPermissions());
        assertEquals(PrincipalIdentityType.API_KEY, identityDao.inserted.getType());
        assertEquals(dto.getApiKey(), identityDao.inserted.getIdentityValue());
        assertEquals(PrincipalCredentialType.API_SECRET, credentialDao.inserted.getCredentialType());
        assertNotEquals(dto.getApiSecret(), credentialDao.inserted.getCredentialValue());
    }

    @Test
    public void shouldResetSecretOnceWithoutChangingApiKey() {
        RecordingOpenClientDao openClientDao = new RecordingOpenClientDao();
        RecordingPrincipalIdentityDao identityDao = new RecordingPrincipalIdentityDao();
        RecordingPrincipalCredentialDao credentialDao = new RecordingPrincipalCredentialDao();
        OpenClientServiceImpl service = new OpenClientServiceImpl(openClientDao, identityDao, credentialDao);
        OpenClientDTO created = service.create(new CreateOpenClientCommand("client", null, null, null, null));
        String oldCredentialValue = credentialDao.inserted.getCredentialValue();

        OpenClientDTO reset = service.resetSecret(new ResetOpenClientSecretCommand(created.getId()));

        assertEquals(created.getApiKey(), reset.getApiKey());
        assertNotEquals(created.getApiSecret(), reset.getApiSecret());
        assertEquals(PrincipalCredentialStatus.ACTIVE, credentialDao.updated.getStatus());
        assertNotEquals(oldCredentialValue, credentialDao.updated.getCredentialValue());
        assertNull(service.get(created.getId()).getApiSecret());
    }

    @Test
    public void shouldCreateApiKeyAndSecretWhenResetSeedClientWithoutIdentity() {
        RecordingOpenClientDao openClientDao = new RecordingOpenClientDao();
        RecordingPrincipalIdentityDao identityDao = new RecordingPrincipalIdentityDao();
        RecordingPrincipalCredentialDao credentialDao = new RecordingPrincipalCredentialDao();
        OpenClientServiceImpl service = new OpenClientServiceImpl(openClientDao, identityDao, credentialDao);
        OpenClient seedClient = new OpenClient();
        seedClient.setName("seed");
        seedClient.setStatus(OpenClientStatus.ENABLED);
        OpenClientId seedClientId = openClientDao.insert(seedClient);

        OpenClientDTO reset = service.resetSecret(new ResetOpenClientSecretCommand(seedClientId));

        assertEquals(seedClientId, reset.getId());
        assertTrue(reset.getApiKey().startsWith("swak_"));
        assertTrue(reset.getApiSecret().startsWith("swas_"));
        assertEquals(PrincipalIdentityType.API_KEY, identityDao.inserted.getType());
        assertEquals(reset.getApiKey(), identityDao.inserted.getIdentityValue());
        assertEquals(PrincipalCredentialType.API_SECRET, credentialDao.inserted.getCredentialType());
        assertNotEquals(reset.getApiSecret(), credentialDao.inserted.getCredentialValue());
    }

    @Test
    public void shouldUpdateStatusAndReplacePermissions() {
        RecordingOpenClientDao openClientDao = new RecordingOpenClientDao();
        OpenClientServiceImpl service = new OpenClientServiceImpl(
                openClientDao, new RecordingPrincipalIdentityDao(), new RecordingPrincipalCredentialDao());
        OpenClientDTO created = service.create(new CreateOpenClientCommand("client", null, null, null, null));

        OpenClientDTO updated = service.change(new UpdateOpenClientCommand(
                created.getId(),
                "renamed",
                OpenClientStatus.DISABLED,
                "[]",
                null,
                "changed",
                Arrays.asList("a", "a", "b")));

        assertEquals("renamed", updated.getName());
        assertEquals(OpenClientStatus.DISABLED, updated.getStatus());
        assertEquals(Arrays.asList("a", "b"), updated.getPermissions());
        assertFalse(openClientDao.permissions.get(created.getId().value()).isEmpty());
    }

    @Test
    public void shouldPageOpenClientsWithoutSecret() {
        RecordingOpenClientDao openClientDao = new RecordingOpenClientDao();
        OpenClientServiceImpl service = new OpenClientServiceImpl(
                openClientDao, new RecordingPrincipalIdentityDao(), new RecordingPrincipalCredentialDao());
        OpenClientDTO created = service.create(new CreateOpenClientCommand("client", null, null, null, null));
        OpenClientQuery query = new OpenClientQuery();
        query.setStatus(OpenClientStatus.ENABLED);

        OpenClientDTO record = service.page(query, new com.github.thundax.common.page.PageQuery(2, 20))
                .getRecords()
                .get(0);

        assertEquals(created.getId(), record.getId());
        assertEquals("ENABLED", openClientDao.status);
        assertEquals(2, openClientDao.pageNo);
        assertEquals(20, openClientDao.pageSize);
        assertNull(record.getApiSecret());
    }

    @Test
    public void shouldChangeOpenClientStatus() {
        RecordingOpenClientDao openClientDao = new RecordingOpenClientDao();
        OpenClientServiceImpl service = new OpenClientServiceImpl(
                openClientDao, new RecordingPrincipalIdentityDao(), new RecordingPrincipalCredentialDao());
        OpenClientDTO created = service.create(new CreateOpenClientCommand("client", null, null, null, null));

        service.changeStatus(new ChangeOpenClientStatusCommand(created.getId(), OpenClientStatus.DISABLED));

        assertEquals(
                OpenClientStatus.DISABLED,
                openClientDao.clients.get(created.getId().value()).getStatus());
    }

    private static class RecordingOpenClientDao implements OpenClientDao {

        private long nextId = 9001L;
        private final Map<Long, OpenClient> clients = new LinkedHashMap<>();
        private final Map<Long, List<OpenClientPermission>> permissions = new LinkedHashMap<>();
        private String status;
        private int pageNo;
        private int pageSize;

        @Override
        public OpenClient getById(OpenClientId id) {
            return clients.get(id.value());
        }

        @Override
        public List<OpenClientPermission> listPermissionsByClientId(OpenClientId clientId) {
            List<OpenClientPermission> result = permissions.get(clientId.value());
            return result == null ? new ArrayList<OpenClientPermission>() : result;
        }

        @Override
        public Page<OpenClient> page(String name, String status, int pageNo, int pageSize) {
            this.status = status;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            Page<OpenClient> page = new Page<>(pageNo, pageSize);
            page.setRecords(new ArrayList<>(clients.values()));
            page.setTotal(clients.size());
            return page;
        }

        @Override
        public OpenClientId insert(OpenClient entity) {
            OpenClientId id = OpenClientId.of(nextId++);
            entity.setId(id);
            clients.put(id.value(), entity);
            return id;
        }

        @Override
        public int update(OpenClient entity) {
            clients.put(entity.getId().value(), entity);
            return 1;
        }

        @Override
        public int updateStatus(OpenClient entity) {
            clients.put(entity.getId().value(), entity);
            return 1;
        }

        @Override
        public int deleteByClientId(OpenClientId clientId) {
            permissions.remove(clientId.value());
            return 1;
        }

        @Override
        public int batchInsertPermissions(List<OpenClientPermission> permissions) {
            for (OpenClientPermission permission : permissions) {
                this.permissions
                        .computeIfAbsent(permission.getClientId().value(), key -> new ArrayList<>())
                        .add(permission);
            }
            return permissions.size();
        }
    }

    private static class RecordingPrincipalIdentityDao implements PrincipalIdentityDao {

        private PrincipalIdentity inserted;
        private long nextId = 1001L;

        @Override
        public PrincipalIdentity getById(PrincipalIdentityId id) {
            return inserted;
        }

        @Override
        public PrincipalIdentity getByIdentity(PrincipalIdentityType identityType, String identityValue) {
            return inserted;
        }

        @Override
        public PrincipalIdentity getByPrincipalKeyAndType(
                PrincipalKey principalKey, PrincipalIdentityType identityType) {
            return inserted;
        }

        @Override
        public List<PrincipalIdentity> listByPrincipalKeyAndStatus(
                PrincipalKey principalKey, PrincipalIdentityStatus status) {
            return inserted == null ? new ArrayList<PrincipalIdentity>() : Arrays.asList(inserted);
        }

        @Override
        public PrincipalIdentityId insert(PrincipalIdentity principalIdentity) {
            PrincipalIdentityId id = PrincipalIdentityId.of(nextId++);
            principalIdentity.setId(id);
            inserted = principalIdentity;
            return id;
        }

        @Override
        public int update(PrincipalIdentity principalIdentity) {
            inserted = principalIdentity;
            return 1;
        }

        @Override
        public int updateStatus(PrincipalIdentity principalIdentity) {
            inserted = principalIdentity;
            return 1;
        }
    }

    private static class RecordingPrincipalCredentialDao implements PrincipalCredentialDao {

        private PrincipalCredential inserted;
        private PrincipalCredential updated;
        private long nextId = 2001L;

        @Override
        public PrincipalCredential getById(PrincipalCredentialId id) {
            return inserted;
        }

        @Override
        public PrincipalCredential getByIdentityIdAndType(
                PrincipalIdentityId identityId, PrincipalCredentialType credentialType) {
            return inserted;
        }

        @Override
        public PrincipalCredential getByPrincipalKeyAndType(
                PrincipalKey principalKey, PrincipalCredentialType credentialType) {
            return inserted;
        }

        @Override
        public List<PrincipalCredential> listByPrincipalKeyAndStatus(
                PrincipalKey principalKey, PrincipalCredentialStatus status) {
            return inserted == null ? new ArrayList<PrincipalCredential>() : Arrays.asList(inserted);
        }

        @Override
        public PrincipalCredentialId insert(PrincipalCredential principalCredential) {
            PrincipalCredentialId id = PrincipalCredentialId.of(nextId++);
            principalCredential.setId(id);
            inserted = principalCredential;
            return id;
        }

        @Override
        public int update(PrincipalCredential principalCredential) {
            updated = principalCredential;
            inserted = principalCredential;
            return 1;
        }

        @Override
        public int updateStatus(PrincipalCredential principalCredential) {
            updated = principalCredential;
            inserted = principalCredential;
            return 1;
        }

        @Override
        public int updateVerifyState(PrincipalCredential principalCredential) {
            updated = principalCredential;
            inserted = principalCredential;
            return 1;
        }
    }
}
