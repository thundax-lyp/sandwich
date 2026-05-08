package com.github.thundax.architecture;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.cache.CacheDTO;
import com.github.thundax.modules.assist.persistence.dao.AsyncTaskDaoImpl;
import com.github.thundax.modules.auth.entity.PreAuthSession;
import com.github.thundax.modules.auth.persistence.dao.AuthSessionRuntimeDaoImpl;
import com.github.thundax.modules.auth.persistence.dao.PreAuthSessionDaoImpl;
import com.github.thundax.modules.auth.persistence.dao.PrincipalAccessTokenDaoImpl;
import com.github.thundax.modules.auth.persistence.dao.PrincipalAuthSessionDaoImpl;
import com.github.thundax.modules.auth.persistence.dao.PrincipalRefreshTokenDaoImpl;
import com.github.thundax.modules.storage.persistence.cache.StorageCacheSupport;
import com.github.thundax.modules.sys.persistence.cache.DepartmentCacheSupport;
import com.github.thundax.modules.sys.persistence.cache.DictCacheSupport;
import com.github.thundax.modules.sys.persistence.cache.MenuCacheSupport;
import com.github.thundax.modules.sys.persistence.cache.RoleCacheSupport;
import com.github.thundax.modules.sys.persistence.cache.UserCacheSupport;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import org.junit.Test;

public class CacheDTOArchitectureTest {

    private static final Class<?>[] CACHE_BOUNDARY_CLASSES = {
        UserCacheSupport.class,
        RoleCacheSupport.class,
        MenuCacheSupport.class,
        DepartmentCacheSupport.class,
        DictCacheSupport.class,
        StorageCacheSupport.class,
        PrincipalAccessTokenDaoImpl.class,
        PrincipalAuthSessionDaoImpl.class,
        PrincipalRefreshTokenDaoImpl.class,
        AuthSessionRuntimeDaoImpl.class,
        PreAuthSessionDaoImpl.class,
        AsyncTaskDaoImpl.class
    };

    @Test
    public void shouldKeepDomainObjectsOutOfJdkSerializationContract() {
        assertFalse(Serializable.class.isAssignableFrom(PreAuthSession.class));
    }

    @Test
    public void shouldDefineSerializableCacheDTOInsideCacheBoundary() throws Exception {
        for (Class<?> boundaryClass : CACHE_BOUNDARY_CLASSES) {
            Class<?> cacheDTOClass = cacheDTOClass(boundaryClass);
            assertTrue(
                    cacheDTOClass.getName() + " must implement CacheDTO",
                    CacheDTO.class.isAssignableFrom(cacheDTOClass));
            serialize(newInstance(cacheDTOClass));
        }
    }

    private Class<?> cacheDTOClass(Class<?> boundaryClass) {
        for (Class<?> nestedClass : boundaryClass.getDeclaredClasses()) {
            if (nestedClass.getSimpleName().endsWith("CacheDTO")) {
                return nestedClass;
            }
        }
        throw new AssertionError(boundaryClass.getName() + " must define an inner CacheDTO");
    }

    private Object newInstance(Class<?> clazz) throws Exception {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private void serialize(Object value) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(output)) {
            objectOutputStream.writeObject(value);
        }
    }
}
