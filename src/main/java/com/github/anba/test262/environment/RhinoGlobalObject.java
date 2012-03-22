/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.environment;

import static com.github.anba.test262.util.Reflection.__init__;
import static org.mozilla.javascript.Context.getCurrentContext;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.TopLevel;
import org.mozilla.javascript.Undefined;
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

    @Override
    @JSFunction("$ERROR")
    public void error(String message) {
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

    @Override
    @JSFunction("runTestCase")
    public void runTestCase(Object testcase) {
        Callable fn = (Callable) testcase;
        Object value = fn.call(getCurrentContext(), this, Undefined.instance,
                new Object[] {});
        if (!Context.toBoolean(value)) {
            failure(getDescription());
        }
    }
}
