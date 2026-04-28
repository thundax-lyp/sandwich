package com.github.thundax.common.cache.config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class JetCacheAutoConfigurationTest {

    @Test
    public void shouldCustomizeGlobalCacheConfigWithProjectDefaults() {
        BeanPostProcessor beanPostProcessor = JetCacheAutoConfiguration.jetCacheGlobalConfigBeanPostProcessor();
        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();

        Object processed = beanPostProcessor.postProcessAfterInitialization(globalCacheConfig, "globalCacheConfig");

        assertSame(globalCacheConfig, processed);
        assertArrayEquals(new String[] {JetCacheAutoConfiguration.BASE_PACKAGE}, globalCacheConfig.getHiddenPackages());
        assertTrue(globalCacheConfig.isAreaInCacheName());
        assertTrue(globalCacheConfig.isPenetrationProtect());
        assertEquals(
                JetCacheAutoConfiguration.DEFAULT_STAT_INTERVAL_MINUTES, globalCacheConfig.getStatIntervalMinutes());
    }

    @Test
    public void shouldKeepExplicitStatIntervalMinutes() {
        BeanPostProcessor beanPostProcessor = JetCacheAutoConfiguration.jetCacheGlobalConfigBeanPostProcessor();
        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setStatIntervalMinutes(5);

        beanPostProcessor.postProcessAfterInitialization(globalCacheConfig, "globalCacheConfig");

        assertEquals(5, globalCacheConfig.getStatIntervalMinutes());
    }
}
