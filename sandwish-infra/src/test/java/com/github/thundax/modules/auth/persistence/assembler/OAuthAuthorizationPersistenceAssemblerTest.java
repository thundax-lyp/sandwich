package com.github.thundax.modules.auth.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.auth.entity.OAuthAuthorization;
import com.github.thundax.modules.auth.persistence.dataobject.OAuthAuthorizationDO;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import org.junit.Test;

public class OAuthAuthorizationPersistenceAssemblerTest {

    @Test
    public void shouldMapOAuthAuthorizationEntityToDataObject() {
        Date issuedAt = new Date(1000L);
        Date expireAt = new Date(2000L);
        OAuthAuthorization entity = new OAuthAuthorization();
        entity.setId(EntityIdCodec.toDomain("authorization-1"));
        entity.setAuthorizationCode("code-1");
        entity.setClientId("admin-web");
        entity.setTenantId("tenant-1");
        entity.setUserId(EntityIdCodec.toDomain("user-1"));
        entity.setRedirectUri("http://127.0.0.1/callback");
        entity.setScopes(new LinkedHashSet<>(Arrays.asList("openid", "profile")));
        entity.setState("state-1");
        entity.setCodeChallenge("challenge-1");
        entity.setCodeChallengeMethod("S256");
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(expireAt);
        entity.setUsed(true);

        OAuthAuthorizationDO dataObject = OAuthAuthorizationPersistenceAssembler.toDataObject(entity);

        assertEquals("authorization-1", dataObject.getId());
        assertEquals("code-1", dataObject.getAuthorizationCode());
        assertEquals("admin-web", dataObject.getClientId());
        assertEquals("tenant-1", dataObject.getTenantId());
        assertEquals("user-1", dataObject.getUserId());
        assertEquals("[\"openid\",\"profile\"]", dataObject.getScopes());
        assertEquals("state-1", dataObject.getState());
        assertEquals("challenge-1", dataObject.getCodeChallenge());
        assertEquals("S256", dataObject.getCodeChallengeMethod());
        assertEquals(issuedAt, dataObject.getIssuedAt());
        assertEquals(expireAt, dataObject.getExpireAt());
        assertTrue(dataObject.isUsed());
    }

    @Test
    public void shouldMapOAuthAuthorizationDataObjectToEntity() {
        OAuthAuthorizationDO dataObject = new OAuthAuthorizationDO();
        dataObject.setId("authorization-1");
        dataObject.setAuthorizationCode("code-1");
        dataObject.setClientId("admin-web");
        dataObject.setTenantId("tenant-1");
        dataObject.setUserId("user-1");
        dataObject.setRedirectUri("http://127.0.0.1/callback");
        dataObject.setScopes("[\"openid\",\"profile\"]");
        dataObject.setExpireAt(new Date(2000L));
        dataObject.setUsed(false);

        OAuthAuthorization entity = OAuthAuthorizationPersistenceAssembler.toEntity(dataObject);

        assertEquals("authorization-1", entity.getId().value());
        assertEquals("code-1", entity.getAuthorizationCode());
        assertEquals("admin-web", entity.getClientId());
        assertTrue(entity.getScopes().contains("openid"));
        assertTrue(entity.canConsume(new Date(1000L)));
        assertFalse(entity.canConsume(new Date(3000L)));
    }
}
