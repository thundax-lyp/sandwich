package com.github.thundax.modules.auth.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.OAuthRefreshToken;
import com.github.thundax.modules.auth.entity.enums.OAuthRefreshTokenStatus;
import com.github.thundax.modules.auth.persistence.dataobject.OAuthRefreshTokenDO;
import java.util.Date;
import org.junit.Test;

public class OAuthRefreshTokenPersistenceAssemblerTest {

    @Test
    public void shouldMapOAuthRefreshTokenEntityToDataObject() {
        Date issuedAt = new Date(1000L);
        Date expireAt = new Date(2000L);
        OAuthRefreshToken entity = new OAuthRefreshToken();
        entity.setId(EntityId.of(4004L));
        entity.setTokenId("token-id-1");
        entity.setTokenHash("hash-1");
        entity.setAccessTokenId("4003");
        entity.setClientId("admin-web");
        entity.setUserId(EntityId.of(1001L));
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(expireAt);
        entity.setStatus(OAuthRefreshTokenStatus.ACTIVE);

        OAuthRefreshTokenDO dataObject = OAuthRefreshTokenPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(4004L), dataObject.getId());
        assertEquals("token-id-1", dataObject.getTokenId());
        assertEquals("hash-1", dataObject.getTokenHash());
        assertEquals("4003", dataObject.getAccessTokenId());
        assertEquals("admin-web", dataObject.getClientId());
        assertEquals(Long.valueOf(1001L), dataObject.getUserId());
        assertEquals(issuedAt, dataObject.getIssuedAt());
        assertEquals(expireAt, dataObject.getExpireAt());
        assertEquals("ACTIVE", dataObject.getStatus());
    }

    @Test
    public void shouldMapOAuthRefreshTokenDataObjectToEntity() {
        OAuthRefreshTokenDO dataObject = new OAuthRefreshTokenDO();
        dataObject.setId(4004L);
        dataObject.setTokenId("token-id-1");
        dataObject.setTokenHash("hash-1");
        dataObject.setAccessTokenId("4003");
        dataObject.setClientId("admin-web");
        dataObject.setUserId(1001L);
        dataObject.setIssuedAt(new Date(1000L));
        dataObject.setExpireAt(new Date(2000L));
        dataObject.setStatus("used");

        OAuthRefreshToken entity = OAuthRefreshTokenPersistenceAssembler.toEntity(dataObject);

        assertEquals(Long.valueOf(4004L), entity.getId().value());
        assertEquals("token-id-1", entity.getTokenId());
        assertEquals("hash-1", entity.getTokenHash());
        assertEquals(Long.valueOf(1001L), entity.getUserId().value());
        assertSame(OAuthRefreshTokenStatus.USED, entity.getStatus());
        assertFalse(entity.canRefresh(new Date(1000L)));

        entity.setStatus(OAuthRefreshTokenStatus.ACTIVE);
        assertTrue(entity.canRefresh(new Date(1000L)));
        assertFalse(entity.canRefresh(new Date(3000L)));
    }
}
