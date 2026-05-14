package com.github.thundax.modules.storage.converter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.autoconfigure.SandwishProperties;
import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.context.SandwishSubject;
import com.github.thundax.common.security.context.SandwishSubjectType;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectIdCodec;
import com.github.thundax.modules.storage.service.StorageService;
import java.util.Collections;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class StorageConverterTest {

    @After
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SandwishContextHolder.clear();
    }

    @Test
    public void shouldBuildPreviewUrlWithContextPath() {
        bindContextPath("/admin-api");
        bindToken("token-1");
        StorageConverter converter = converter();
        StoredObject storage = storage();

        assertEquals("/admin-api/api/storage/object/1001/content?token=token-1", converter.toPreviewUrl(storage));
    }

    @Test
    public void shouldPrefixStoredLocalAccessEndpointWithContextPath() {
        bindContextPath("/admin-api");
        bindToken("token-1");
        StorageConverter converter = converter();
        StoredObject storage = storage();
        storage.setAccessEndpoint("/api/storage/object/1001/content");

        assertEquals("/admin-api/api/storage/object/1001/content?token=token-1", converter.toPreviewUrl(storage));
    }

    @Test
    public void shouldNotAppendDuplicateToken() {
        bindContextPath("/admin-api");
        bindToken("token-2");
        StorageConverter converter = converter();
        StoredObject storage = storage();
        storage.setAccessEndpoint("/api/storage/object/1001/content?token=token-1");

        assertEquals("/admin-api/api/storage/object/1001/content?token=token-1", converter.toPreviewUrl(storage));
    }

    @Test
    public void shouldResolveEntityFromTokenizedPreviewUrl() {
        StorageService storageService = mock(StorageService.class);
        StoredObject storage = storage();
        when(storageService.get(StoredObjectIdCodec.toDomain(1001L))).thenReturn(storage);
        StorageConverter converter = new StorageConverter(new SandwishProperties(), storageService);

        assertEquals(storage, converter.toEntity("/admin-api/api/storage/object/1001/content?token=token-1"));
    }

    private void bindContextPath(String contextPath) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(contextPath);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void bindToken(String token) {
        SandwishContextHolder.setSubject(
                new SandwishSubject("user-1", SandwishSubjectType.ADMIN_USER, "tester", token, Collections.emptySet()));
    }

    private StorageConverter converter() {
        return new StorageConverter(new SandwishProperties(), mock(StorageService.class));
    }

    private StoredObject storage() {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectIdCodec.toDomain(1001L));
        return storage;
    }
}
