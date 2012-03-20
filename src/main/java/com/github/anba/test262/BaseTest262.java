/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262;

import static com.github.anba.test262.util.Functional.iterable;
import static com.github.anba.test262.util.Functional.map;
import static com.github.anba.test262.util.Resources.loadConfiguration;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.instanceOf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.github.anba.test262.environment.Environment;
import com.github.anba.test262.util.EcmaErrorMatcher;
import com.github.anba.test262.util.LazyInit;
import com.github.anba.test262.util.Resources;
import com.github.anba.test262.util.Test262Info;
import com.github.anba.test262.util.Functional.Mapper;

/**
 * Base class for all test262 test suite classes
 * 
 * @author André Bargull
 * 
 */
public abstract class BaseTest262 {
    private final String testsuite;
    private final String sourceName;
    private final String path;

    // from configuration
    private final boolean strictSupported;
    private final String encoding;

    protected BaseTest262(Configuration configuration, String testsuite,
            String sourceName, String path) {
        Configuration c = configuration.subset(testsuite);
        this.testsuite = testsuite;
        this.sourceName = sourceName;
        this.path = path;
        this.strictSupported = c.getBoolean("strict", false);
        this.encoding = c.getString("encoding", "UTF-8");
    }

    /**
     * Returns the test suite name
     */
    public final String getTestsuite() {
        return testsuite;
    }

    /**
     * Returns the test source name
     */
    public final String getSourceName() {
        return sourceName;
    }

    /**
     * Returns the test path
     */
    public final String getPath() {
        return path;
    }

    /**
     * Returns {@code true} iff strict mode semantics are supported
     */
    protected final boolean isStrictSupported() {
        return strictSupported;
    }

    protected final Test262Info info() throws IOException {
        Path path = Paths.get(getPath());
        return Test262Info.from(path, encoding);
    }

    protected final void execute(Environment<?> environment)
            throws IOException {
        Path path = Paths.get(getPath());
        InputStream source = Files.newInputStream(path);
        environment.eval(getSourceName(), source);
    }

    protected static List<Object[]> collectTestCases(Configuration configuration)
            throws IOException {
        // base directory to search for test javascript files
        String testpath = configuration.getString("");

        // set of test-case id to exclude from testing
        List<?> values = configuration.getList("exclude", emptyList());

        // if 'true' only the excluded test cases are used, otherwise they're
        // omitted
        boolean only_excluded = configuration
                .getBoolean("only_excluded", false);

        // optional exclusion pattern
        String excludeRE = configuration.getString("exclude_re", "");
        Pattern exclude = Pattern.compile(excludeRE);

        return Resources.collectTestCases(testpath, values, only_excluded,
                exclude);
    }

    protected static LazyInit<Configuration> newConfiguration() {
        return new LazyInit<Configuration>() {
            @Override
            protected Configuration initialize() {
                Configuration config = loadConfiguration("resource:test262.properties");
                // test load for required property "test262"
                config.getString("test262");
                return config;
            }
        };
    }

    protected static final <T extends Throwable> Matcher<T> hasErrorType(
            final String errorType, final EcmaErrorMatcher<T> matcher) {
        return new TypeSafeMatcher<T>(matcher.exception()) {
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText(String.format(
                        "exception with error-type '%s'", errorType));
            }

            @Override
            public boolean matchesSafely(T error) {
                return matcher.matches(error, errorType);
            }
        };
    }

    /**
     * anyOf(asList(types).map(x -> instanceOf(x)))
     */
    protected static final Matcher<Object> anyInstanceOf(
            final Class<?>... types) {
        return anyOf(map(iterable(types),
                new Mapper<Class<?>, Matcher<? super Object>>() {
                    @Override
                    public Matcher<? super Object> map(Class<?> type) {
                        return instanceOf(type);
                    }
                }));
    }

}
