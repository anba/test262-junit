/**
 * Copyright (c) 2011-2012 André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/test262-junit>
 */
package com.github.anba.test262.util;

/**
 * 
 * 
 * @author André Bargull
 */
public interface EcmaErrorMatcher<T extends Throwable> {
    Class<? extends T> exception();

    boolean matches(T error, String errorType);
}
