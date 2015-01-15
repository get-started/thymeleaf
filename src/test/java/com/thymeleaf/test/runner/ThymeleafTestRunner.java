package com.thymeleaf.test.runner;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.thymeleaf.testing.templateengine.engine.TestExecutor;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by L.x on 15-1-15.
 */
public class ThymeleafTestRunner extends ParentRunner<ThymeleafTestRunner.Test> {

    private String resource;

    public static interface Test {

        void run() throws Throwable;
    }

    public ThymeleafTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        Resource meta = testClass.getAnnotation(Resource.class);
        assertThat("@Resource not annotated!",meta, notNullValue());
        assertThat("@Resource.value not specified!",meta.value(), allOf(not(""), notNullValue()));
        resource = meta.value();
    }

    @Override
    protected List<Test> getChildren() {
        return Arrays.asList((Test) new Test() {
            @Override
            public void run() throws Throwable {
                TestExecutor executor = new TestExecutor();

                executor.execute(resource);
                assertTrue(executor.isAllOK());
            }
        });
    }

    @Override
    protected Description describeChild(Test child) {
        return Description.createTestDescription(getTestClass().getJavaClass(), "resource => [" + resource + "]");
    }

    @Override
    protected void runChild(Test test, RunNotifier notifier) {
        Description description = describeChild(test);
        notifier.fireTestStarted(description);
        try {
            test.run();
        } catch (AssumptionViolatedException e) {
            notifier.fireTestAssumptionFailed(new Failure(description, e));
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(description, e));
        } finally {
            notifier.fireTestFinished(description);
        }
    }


}
