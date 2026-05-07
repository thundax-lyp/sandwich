package com.github.thundax.modules.auth.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.OAuthAccessToken;
import com.github.thundax.modules.auth.entity.enums.OAuthAccessTokenStatus;
import com.github.thundax.modules.auth.persistence.dataobject.OAuthAccessTokenDO;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import org.junit.Test;

public class OAuthAccessTokenPersistenceAssemblerTest {

    @Test
    public void shouldMapOAuthAccessTokenEntityToDataObject() {
        Date issuedAt = new Date(1000L);
        Date expireAt = new Date(2000L);
        OAuthAccessToken entity = new OAuthAccessToken();
        entity.setId(EntityId.of(4003L));
        entity.setTokenId("token-id-1");
        entity.setTokenHash("hash-1");
        entity.setClientId("admin-web");
        entity.setUserId(EntityId.of(1001L));
        entity.setScopes(new LinkedHashSet<>(Arrays.asList("openid", "profile")));
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(expireAt);
        entity.setStatus(OAuthAccessTokenStatus.ACTIVE);

        OAuthAccessTokenDO dataObject = OAuthAccessTokenPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(4003L), dataObject.getId());
        assertEquals("token-id-1", dataObject.getTokenId());
        assertEquals("hash-1", dataObject.getTokenHash());
        assertEquals("admin-web", dataObject.getClientId());
        assertEquals(Long.valueOf(1001L), dataObject.getUserId());
        assertEquals("[\"openid\",\"profile\"]", dataObject.getScopes());
        assertEquals(issuedAt, dataObject.getIssuedAt());
        assertEquals(expireAt, dataObject.getExpireAt());
        assertEquals("ACTIVE", dataObject.getStatus());
    }

    @Test
    public void shouldMapOAuthAccessTokenDataObjectToEntity() {
        OAuthAccessTokenDO dataObject = new OAuthAccessTokenDO();
        dataObject.setId(4003L);
        dataObject.setTokenId("token-id-1");
        dataObject.setTokenHash("hash-1");
        dataObject.setClientId("admin-web");
        dataObject.setUserId(1001L);
        dataObject.setScopes("[\"openid\",\"profile\"]");
        dataObject.setIssuedAt(new Date(1000L));
        dataObject.setExpireAt(new Date(2000L));
        dataObject.setStatus("revoked");

        OAuthAccessToken entity = OAuthAccessTokenPersistenceAssembler.toEntity(dataObject);

        assertEquals(Long.valueOf(4003L), entity.getId().value());
        assertEquals("token-id-1", entity.getTokenId());
        assertEquals("hash-1", entity.getTokenHash());
        assertTrue(entity.getScopes().contains("openid"));
        assertSame(OAuthAccessTokenStatus.REVOKED, entity.getStatus());
        assertFalse(entity.isIntrospectionActive(new Date(1000L)));

        entity.setStatus(OAuthAccessTokenStatus.ACTIVE);
        assertTrue(entity.isIntrospectionActive(new Date(1000L)));
        assertFalse(entity.isIntrospectionActive(new Date(3000L)));
    }
}
