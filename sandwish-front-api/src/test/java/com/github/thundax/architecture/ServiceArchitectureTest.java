package com.github.thundax.architecture;

import com.github.thundax.common.test.architecture.AbstractArchitectureTest;
import com.github.thundax.common.test.architecture.LayerArchitectureRuleSupport;
import org.junit.Test;

public class ServiceArchitectureTest extends AbstractArchitectureTest {

    @Test
    public void shouldOnlyDeclareAuthServicesInApiModule() {
        LayerArchitectureRuleSupport.assertApiModuleSourceDeclaresOnlyAuthServices("sandwish-front-api");
    }
}
