/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262;

import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import com.github.anba.test262.environment.Environment;
import com.github.anba.test262.environment.Environments;
import com.github.anba.test262.environment.Environments.EnvironmentProvider;
import com.github.anba.test262.environment.GlobalObject;
import com.github.anba.test262.util.ExceptionHandler;
import com.github.anba.test262.util.LabelledParameterized;
import com.github.anba.test262.util.LazyInit;
import com.github.anba.test262.util.Test262AssertionError;
import com.github.anba.test262.util.Test262Info;

/**
 * The new test262 style
 * 
 * @author André Bargull
 * 
 */
@RunWith(LabelledParameterized.class)
public final class Test262 extends BaseTest262 {
    private static final String TEST_SUITE = "test.suite.test262";

    private static final LazyInit<Configuration> configuration = newConfiguration();
    private static EnvironmentProvider<GlobalObject> provider;
    private Environment<GlobalObject> environment;

    public Test262(String sourceName, String path) {
        super(configuration.get(), TEST_SUITE, sourceName, path);
    }

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Rule
    public ExceptionHandler handler = new ExceptionHandler() {
        @Override
        protected void handle(Throwable t) {
            throw new AssertionError(t.getMessage(), t);
        }
    };

    @Test
    public void test() throws Throwable {
        Test262Info info = info();
        if (info.isOnlyStrict() || info.isNoStrict()) {
            assumeTrue(isStrictSupported());
        }

        Matcher<Object> m = anyInstanceOf(environment.exceptions());
        if (info.isNegative()) {
            m = either(m).or(instanceOf(Test262AssertionError.class));
            expected.expect(m);
            String errorType = info.getErrorType();
            if (errorType != null) {
                expected.expect(hasErrorType(errorType,
                        environment.matcher(errorType)));
            }
        } else {
            handler.match(m);
        }

        execute(environment);
    }

    @Before
    public void setUp() throws IOException {
        Environment<GlobalObject> env = provider.environment(TEST_SUITE,
                getSourceName(), info());
        env.global().include("sta.js");
        environment = env;
    }

    @After
    public void tearDown() {
        environment.exit();
    }

    @BeforeClass
    public static void setUpClass() {
        provider = Environments.get(configuration.get());
    }

    @Parameters
    public static List<Object[]> files() throws IOException {
        return collectTestCases(configuration.get().subset(TEST_SUITE));
    }
}
