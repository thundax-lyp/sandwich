package com.github.thundax.common.test.architecture;

import java.nio.file.Path;
import org.junit.Test;

public class MethodBodyArchitectureRuleSupportTest {

    @Test
    public void shouldRejectUnannotatedOneLinePrivateAndProtectedMethodsInEntryAndImplementationClasses()
            throws Exception {
        MethodBodyArchitectureRuleSupport.assertPrivateAndProtectedMethodsAreNotOneLine(repositoryRoot());
    }

    private static Path repositoryRoot() {
        return ArchitectureSourceSupport.repositoryRoot();
    }
}
