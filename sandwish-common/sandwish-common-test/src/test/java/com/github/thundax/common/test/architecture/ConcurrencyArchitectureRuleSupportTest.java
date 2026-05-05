package com.github.thundax.common.test.architecture;

import com.github.thundax.common.test.architecture.fixture.concurrency.invalid.CompletableFutureAsyncFixture;
import com.github.thundax.common.test.architecture.fixture.concurrency.invalid.ExecutorsFactoryFixture;
import com.github.thundax.common.test.architecture.fixture.concurrency.invalid.RawThreadFixture;
import com.github.thundax.common.test.architecture.fixture.concurrency.valid.ManagedExecutorFixture;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

public class ConcurrencyArchitectureRuleSupportTest {

    @Test
    public void shouldAcceptManagedExecutorUsage() {
        JavaClasses classes = new ClassFileImporter().importClasses(ManagedExecutorFixture.class);

        ConcurrencyArchitectureRuleSupport.shouldNotCreateRawThreads(
                        ManagedExecutorFixture.class.getPackage().getName())
                .check(classes);
        ConcurrencyArchitectureRuleSupport.shouldNotUseExecutorsFactory(
                        ManagedExecutorFixture.class.getPackage().getName())
                .check(classes);
        ConcurrencyArchitectureRuleSupport.shouldNotUseCompletableFutureAsyncWithoutExecutor(
                        ManagedExecutorFixture.class.getPackage().getName())
                .check(classes);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectRawThreadCreation() {
        JavaClasses classes = new ClassFileImporter().importClasses(RawThreadFixture.class);

        ConcurrencyArchitectureRuleSupport.shouldNotCreateRawThreads(
                        RawThreadFixture.class.getPackage().getName())
                .check(classes);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectExecutorsFactory() {
        JavaClasses classes = new ClassFileImporter().importClasses(ExecutorsFactoryFixture.class);

        ConcurrencyArchitectureRuleSupport.shouldNotUseExecutorsFactory(
                        ExecutorsFactoryFixture.class.getPackage().getName())
                .check(classes);
    }

    @Test(expected = AssertionError.class)
    public void shouldRejectCompletableFutureAsyncWithoutExecutor() {
        JavaClasses classes = new ClassFileImporter().importClasses(CompletableFutureAsyncFixture.class);

        ConcurrencyArchitectureRuleSupport.shouldNotUseCompletableFutureAsyncWithoutExecutor(
                        CompletableFutureAsyncFixture.class.getPackage().getName())
                .check(classes);
    }
}
