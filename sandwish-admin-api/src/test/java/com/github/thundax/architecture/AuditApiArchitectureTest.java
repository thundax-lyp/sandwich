package com.github.thundax.architecture;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.modules.audit.controller.AuditController;
import java.lang.reflect.Method;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;

public class AuditApiArchitectureTest {

    @Test
    public void shouldKeepAuditControllerWrappedAndPermissioned() {
        assertTrue(AuditController.class.isAnnotationPresent(WrappedApiController.class));
        assertNotNull(AuditController.class.getAnnotation(RequestMapping.class));

        for (Method method : AuditController.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                assertTrue(
                        method.getName() + " must declare permission", method.isAnnotationPresent(HasPermission.class));
            }
        }
    }
}
