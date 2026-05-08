package com.github.thundax.modules.auth.persistence.assembler;

import static org.junit.Assert.assertEquals;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.auth.entity.PrincipalLoginEvent;
import com.github.thundax.modules.auth.entity.enums.PrincipalAuthenticationMethod;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalLoginEventType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalLoginEventId;
import com.github.thundax.modules.auth.persistence.dataobject.PrincipalLoginEventDO;
import java.util.Date;
import org.junit.Test;

public class PrincipalLoginEventPersistenceAssemblerTest {

    @Test
    public void shouldMapPrincipalLoginEventEntityToDataObject() {
        Date occurredAt = new Date(1000L);
        PrincipalLoginEvent entity = new PrincipalLoginEvent();
        entity.setId(PrincipalLoginEventId.of("abc1"));
        entity.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, EntityId.of(1001L)));
        entity.setClientId("admin-api");
        entity.setEventType(PrincipalLoginEventType.LOGIN_SUCCESS);
        entity.setAuthenticationMethod(PrincipalAuthenticationMethod.PASSWORD);
        entity.setIdentityType(PrincipalIdentityType.USER_ACCOUNT);
        entity.setOccurredAt(occurredAt);
        entity.setIp("127.0.0.1");
        entity.setUserAgent("JUnit");
        entity.setReason(PrincipalLoginEvent.REASON_NONE);

        PrincipalLoginEventDO dataObject = PrincipalLoginEventPersistenceAssembler.toDataObject(entity);

        assertEquals("abc1", dataObject.getId());
        assertEquals("USER", dataObject.getPrincipalType());
        assertEquals(Long.valueOf(1001L), dataObject.getPrincipalId());
        assertEquals("admin-api", dataObject.getClientId());
        assertEquals("LOGIN_SUCCESS", dataObject.getEventType());
        assertEquals("PASSWORD", dataObject.getAuthenticationMethod());
        assertEquals("USER_ACCOUNT", dataObject.getIdentityType());
        assertEquals(occurredAt, dataObject.getOccurredAt());
        assertEquals("127.0.0.1", dataObject.getIp());
        assertEquals("JUnit", dataObject.getUserAgent());
        assertEquals("NONE", dataObject.getReason());
    }

    @Test
    public void shouldMapPrincipalLoginEventDataObjectToEntity() {
        PrincipalLoginEventDO dataObject = new PrincipalLoginEventDO();
        dataObject.setId("abc1");
        dataObject.setPrincipalType("USER");
        dataObject.setPrincipalId(1001L);
        dataObject.setClientId("admin-api");
        dataObject.setEventType("LOGIN_FAILED");
        dataObject.setAuthenticationMethod("PASSWORD");
        dataObject.setIdentityType("USER_ACCOUNT");
        dataObject.setOccurredAt(new Date(1000L));
        dataObject.setReason("INVALID_CREDENTIAL");

        PrincipalLoginEvent entity = PrincipalLoginEventPersistenceAssembler.toEntity(dataObject);

        assertEquals(PrincipalLoginEventId.of("abc1"), entity.getId());
        assertEquals(PrincipalKey.of(PrincipalType.USER, EntityId.of(1001L)), entity.getPrincipalKey());
        assertEquals(PrincipalLoginEventType.LOGIN_FAILED, entity.getEventType());
        assertEquals(PrincipalAuthenticationMethod.PASSWORD, entity.getAuthenticationMethod());
        assertEquals(PrincipalIdentityType.USER_ACCOUNT, entity.getIdentityType());
        assertEquals(PrincipalLoginEvent.REASON_INVALID_CREDENTIAL, entity.getReason());
    }
}
