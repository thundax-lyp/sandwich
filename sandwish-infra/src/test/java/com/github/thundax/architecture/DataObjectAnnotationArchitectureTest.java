package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.ModelAnnotationArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class DataObjectAnnotationArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldEnforceDatabaseDataObjectClassAnnotations() {
        JavaClasses classes = importPackages("com.github.thundax.modules");

        ModelAnnotationArchitectureRuleSupport.dataObjectClassAnnotationsRequired(
                        "com.github.thundax.modules",
                        "com.github.thundax.modules.assist.persistence.dataobject.AsyncTaskDO",
                        "com.github.thundax.modules.auth.persistence.dataobject.AccessTokenDO",
                        "com.github.thundax.modules.auth.persistence.dataobject.LoginFormDO")
                .check(classes);
    }
}
