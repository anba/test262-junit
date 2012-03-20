/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.util;

import java.lang.reflect.Method;

import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;

/**
 * Helper class for some reflection support
 * 
 * @author André Bargull
 * 
 */
public final class Reflection {
    private Reflection() {
    }

    /**
     * Defines a new function property for the method
     */
    public static void __def__(ScriptableObject object, String function,
            Method method) {
        FunctionObject f = new FunctionObject(function, method, object);
        object.defineProperty(function, f, ScriptableObject.DONTENUM
                | ScriptableObject.READONLY, false);
    }

    /**
     * Collects all methods annoted with {@link JSFunction} annotations on
     * {@code object} and defines these as new functions on the scriptable
     * object
     */
    public static void __init__(ScriptableObject object) {
        for (Method m : object.getClass().getMethods()) {
            if (m.isAnnotationPresent(JSFunction.class)) {
                JSFunction f = m.getAnnotation(JSFunction.class);
                __def__(object, f.value(), m);
            }
        }
    }

    /**
     * Creates a new instance for the class-name
     */
    @SuppressWarnings("unchecked")
    public static <T> T __new__(String classname) {
        try {
            return (T) Class.forName(classname).newInstance();
        } catch (InstantiationException | IllegalAccessException
                | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
