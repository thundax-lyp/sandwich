package com.github.thundax.modules.auth.persistence.assembler;

import static org.junit.Assert.*;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.OAuthAuthorization;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
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
        entity.setId(EntityId.of(4002L));
        entity.setAuthorizationCode("code-1");
        entity.setClientId("admin-web");
        entity.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1001L));
        entity.setRedirectUri("http://127.0.0.1/callback");
        entity.setScopes(new LinkedHashSet<>(Arrays.asList("openid", "profile")));
        entity.setState("state-1");
        entity.setCodeChallenge("challenge-1");
        entity.setCodeChallengeMethod("S256");
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(expireAt);
        entity.setUsed(true);

        OAuthAuthorizationDO dataObject = OAuthAuthorizationPersistenceAssembler.toDataObject(entity);

        assertEquals(Long.valueOf(4002L), dataObject.getId());
        assertEquals("code-1", dataObject.getAuthorizationCode());
        assertEquals("admin-web", dataObject.getClientId());
        assertEquals("USER", dataObject.getPrincipalType());
        assertEquals(Long.valueOf(1001L), dataObject.getPrincipalId());
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
        dataObject.setId(4002L);
        dataObject.setAuthorizationCode("code-1");
        dataObject.setClientId("admin-web");
        dataObject.setPrincipalType("USER");
        dataObject.setPrincipalId(1001L);
        dataObject.setRedirectUri("http://127.0.0.1/callback");
        dataObject.setScopes("[\"openid\",\"profile\"]");
        dataObject.setExpireAt(new Date(2000L));
        dataObject.setUsed(false);

        OAuthAuthorization entity = OAuthAuthorizationPersistenceAssembler.toEntity(dataObject);

        assertEquals(Long.valueOf(4002L), entity.getId().value());
        assertEquals("code-1", entity.getAuthorizationCode());
        assertEquals("admin-web", entity.getClientId());
        assertEquals(PrincipalKey.of(PrincipalType.USER, 1001L), entity.getPrincipalKey());
        assertTrue(entity.getScopes().contains("openid"));
        assertTrue(entity.canConsume(new Date(1000L)));
        assertFalse(entity.canConsume(new Date(3000L)));
    }
}
