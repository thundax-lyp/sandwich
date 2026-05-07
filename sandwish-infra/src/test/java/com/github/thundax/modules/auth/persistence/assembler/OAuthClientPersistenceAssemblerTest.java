package com.github.thundax.modules.auth.persistence.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.OAuthClient;
import com.github.thundax.modules.auth.entity.enums.OAuthClientStatus;
import com.github.thundax.modules.auth.persistence.dataobject.OAuthClientDO;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import org.junit.Test;

public class OAuthClientPersistenceAssemblerTest {

    @Test
    public void shouldMapOAuthClientEntityToDataObject() {
        Date createDate = new Date(1000L);
        Date updateDate = new Date(2000L);
        OAuthClient entity = new OAuthClient();
        entity.setId(EntityId.of(4001L));
        entity.setClientId("admin-web");
        entity.setClientSecretHash("secret-hash");
        entity.setClientName("Admin Web");
        entity.setClientType("CONFIDENTIAL");
        entity.setGrantTypes(new LinkedHashSet<>(Arrays.asList("authorization_code", "refresh_token")));
        entity.setScopes(new LinkedHashSet<>(Arrays.asList("openid", "profile")));
        entity.setRedirectUris(new LinkedHashSet<>(Arrays.asList("http://127.0.0.1/callback")));
        entity.setAccessTokenTtlSeconds(7200L);
        entity.setRefreshTokenTtlSeconds(2592000L);
        entity.setStatus(OAuthClientStatus.ENABLED);
        entity.setContact("admin@example.com");
        entity.setRemark("seed client");
        entity.setCreateDate(createDate);
        entity.setCreateUserId("admin");
        entity.setUpdateDate(updateDate);
        entity.setUpdateUserId("operator");

        OAuthClientDO dataObject = OAuthClientPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(4001L), dataObject.getId());
        assertEquals("admin-web", dataObject.getClientId());
        assertEquals("secret-hash", dataObject.getClientSecretHash());
        assertEquals("[\"authorization_code\",\"refresh_token\"]", dataObject.getGrantTypes());
        assertEquals("[\"openid\",\"profile\"]", dataObject.getScopes());
        assertEquals("[\"http://127.0.0.1/callback\"]", dataObject.getRedirectUris());
        assertEquals(7200L, dataObject.getAccessTokenTtlSeconds());
        assertEquals(2592000L, dataObject.getRefreshTokenTtlSeconds());
        assertEquals("ENABLED", dataObject.getStatus());
        assertEquals(createDate, dataObject.getCreateDate());
        assertEquals("admin", dataObject.getCreateBy());
        assertEquals(updateDate, dataObject.getUpdateDate());
        assertEquals("operator", dataObject.getUpdateBy());
    }

    @Test
    public void shouldMapOAuthClientDataObjectToEntity() {
        OAuthClientDO dataObject = new OAuthClientDO();
        dataObject.setId(4001L);
        dataObject.setClientId("admin-web");
        dataObject.setClientSecretHash("secret-hash");
        dataObject.setClientName("Admin Web");
        dataObject.setClientType("CONFIDENTIAL");
        dataObject.setGrantTypes("[\"authorization_code\",\"refresh_token\"]");
        dataObject.setScopes("[\"openid\",\"profile\"]");
        dataObject.setRedirectUris("[\"http://127.0.0.1/callback\"]");
        dataObject.setAccessTokenTtlSeconds(7200L);
        dataObject.setRefreshTokenTtlSeconds(2592000L);
        dataObject.setStatus("disabled");

        OAuthClient entity = OAuthClientPersistenceAssembler.toEntity(dataObject);

        assertEquals(Long.valueOf(4001L), entity.getId().value());
        assertEquals("admin-web", entity.getClientId());
        assertTrue(entity.supportsGrantType("authorization_code"));
        assertTrue(entity.supportsScopes(new LinkedHashSet<>(Arrays.asList("openid", "profile"))));
        assertTrue(entity.supportsRedirectUri("http://127.0.0.1/callback"));
        assertSame(OAuthClientStatus.DISABLED, entity.getStatus());
    }
}
