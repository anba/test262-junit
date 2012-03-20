/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.environment;

import static com.github.anba.test262.util.Reflection.__init__;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.mozilla.javascript.TopLevel;
import org.mozilla.javascript.annotations.JSFunction;

/**
 * Shared global object definition for all test suites. Includes all necessary
 * global function definitions to run sputnik, ietestcenter and canonical
 * test262 test cases.
 * 
 * @author André Bargull
 */
@SuppressWarnings({ "serial" })
public abstract class RhinoGlobalObject extends TopLevel implements
        GlobalObject {
    {
        __init__(this);
    }

    /**
     * Parses and executes the given file
     */
    protected abstract void include(Path file) throws IOException;

    /**
     * Process test failure with message
     */
    protected abstract void failure(String message);

    /**
     * Returns {@code true} iff strict-mode semantics are supported
     */
    protected abstract boolean isStrictSupported();

    /**
     * Returns the current test description
     */
    protected abstract String getDescription();

    // TODO: this is a hack...
    private static final String DEFAULT_ERROR_MSG = "Test case returned non-true value!";

    @Override
    @JSFunction("$ERROR")
    public void error(String message) {
        // intercept $ERROR() calls from runTestCase()
        if (DEFAULT_ERROR_MSG.equals(message)) {
            message = Objects.toString(getDescription(), "assertion error");
        }
        failure(message);
    }

    @Override
    @JSFunction("$FAIL")
    public void fail(String message) {
        failure(message);
    }

    @Override
    @JSFunction("$PRINT")
    public void print(String message) {
        System.out.println(message);
    }

    @Override
    @JSFunction("$INCLUDE")
    public void include(String file) throws IOException {
        include(Paths.get(file));
    }
}
