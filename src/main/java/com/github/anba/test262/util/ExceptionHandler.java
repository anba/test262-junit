/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.util;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

import org.hamcrest.Matcher;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Exception handler like {@link TestRule}
 * 
 * @author André Bargull
 * 
 */
public abstract class ExceptionHandler implements TestRule {
    private Matcher<?> matcher = not(anything());

    public void match(Matcher<?> matcher) {
        requireNonNull(matcher);
        this.matcher = matcher;
    }

    /**
     * To be implemented by subclasses
     */
    protected abstract void handle(Throwable t);

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } catch (Throwable t) {
                    if (matcher.matches(t)) {
                        handle(t);
                    } else {
                        throw t;
                    }
                }
            }
        };
    }
}
