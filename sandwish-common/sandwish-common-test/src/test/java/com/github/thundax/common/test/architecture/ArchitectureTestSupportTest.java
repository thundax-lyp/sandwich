package com.github.thundax.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Assert;
import org.junit.Test;

public class ArchitectureTestSupportTest extends AbstractArchitectureTest {

    @Test
    public void shouldImportArchitectureTestPackage() {
        JavaClasses classes = importPackages("com.github.thundax.common.test.architecture");

        Assert.assertTrue(classes.size() > 0);
    }
}
